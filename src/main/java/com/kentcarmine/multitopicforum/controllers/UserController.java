package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.dtos.UserEmailDto;
import com.kentcarmine.multitopicforum.dtos.UserPasswordDto;
import com.kentcarmine.multitopicforum.dtos.UserSearchDto;
import com.kentcarmine.multitopicforum.events.OnRegistrationCompleteEvent;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.services.EmailService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Controller for handling all user-related tasks (ie. login/logout, registration, password reset, etc)
 */
@Controller
public class UserController {

    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageSource messageSource;
    private final EmailService emailService;


    @Autowired
    public UserController(UserService userService, ApplicationEventPublisher applicationEventPublisher, MessageSource messageSource, EmailService emailService) {
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.messageSource = messageSource;
        this.emailService = emailService;
    }

    /**
     * Display login form if no one is logged in, otherwise navigate to currently logged in user's page
     */
    @GetMapping("/login")
    public String showLoginForm() {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            return "redirect:/users/" + loggedInUser.getUsername();
        }

        return "login-form";
    }

    /**
     * Show the profile page of the user with the given name, or throw a UserNotFoundException if no such user exists
     */
    @GetMapping("/users/{username}")
    public String showUserPage(Model model, @PathVariable String username) {
        if (userService.usernameExists(username)) {
//            SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().forEach((a) -> System.out.println(((GrantedAuthority) a).toString()));
            User user = userService.getUser(username);
            model.addAttribute("user", user);
            model.addAttribute("loggedInUser", userService.getLoggedInUser());
            return "user-page";
        } else {
            throw new UserNotFoundException("User with name " + username + " was not found");
        }
    }

    /**
     * Display the form to allow a user to register. If a user is logged in, instead go to that user's profile page
     */
    @GetMapping("/registerUser")
    public String showUserRegistrationForm(Model model) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            return "redirect:/users/" + loggedInUser.getUsername();
        }

        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "user-registration-form";
    }

    /**
     * Handle processing when a user submits a registration form, either creating the user and sending them an email to
     * complete registration if the input is valid, or informing them of errors in their input if the input is not valid
     */
    @PostMapping("/processUserRegistration")
    public ModelAndView processUserRegistration(@Valid @ModelAttribute("user") UserDto user, BindingResult bindingResult, HttpServletRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            return new ModelAndView("redirect:/users/" + loggedInUser.getUsername());
        }

        updateRegistrationBindingResult(user, bindingResult);

        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("user-registration-form", "user", user);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        } else {
            User registeredUser = userService.createUserByUserDto(user);
            try {
                String appUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString() + request.getContextPath();
                applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(registeredUser, request.getLocale(), appUrl));
            } catch (Exception ex) {
                System.out.println("### Error occurred completing registration ###");
                ex.printStackTrace();
                return new ModelAndView("registration-email-error");
            }

            return new ModelAndView("redirect:/login?regEmailSent");
        }
    }

    /**
     * Handle finalizing registration of a user when they click the link in their registration confirmation email.
     * Creates the account and enables the user if the verification token is valid, otherwise displays an error to
     * the user.
     */
    @GetMapping("/registrationConfirm")
    public ModelAndView confirmRegistration(WebRequest request, @RequestParam("token") String token) {
        Locale locale = request.getLocale();
        VerificationToken verificationToken = userService.getVerificationToken(token);

        ModelAndView mv;

        if (verificationToken == null) {
            String message = messageSource.getMessage("auth.message.invalidToken", null, locale);

            mv = new ModelAndView("registration-confirmation-error", HttpStatus.NOT_FOUND);
            mv.getModel().put("message", message);
            return mv;
        }

        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if (verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0 && user != null && !user.isEnabled()) {
            String messageValue = messageSource.getMessage("auth.message.expired", null, locale);

            mv = new ModelAndView("registration-confirmation-error", HttpStatus.NOT_FOUND);
            mv.getModel().put("message", messageValue);
            mv.getModel().put("expired", true);
            mv.getModel().put("token", token);
            return mv;
        } else if (user != null && user.isEnabled()) {
            mv = new ModelAndView("redirect:/login");
            return mv;
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        mv = new ModelAndView("redirect:/login?registrationSuccess");
        return mv;
    }

    /**
     * Resends the registration email to the user upon request (ie. if the previous registration token is expired).
     * If the user already exsists and is enabled, sends them to the login page.
     */
    @GetMapping("/resendRegistrationEmail")
    public String resendRegistrationEmail(HttpServletRequest request, @RequestParam("token") String existingToken) {
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);

        User user = userService.getUserByVerificationToken(newToken.getToken());

        if (user.isEnabled()) {
            return "redirect:/login";
        }

        sendResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, user);

        return "redirect:/login?regEmailSent";
    }

    /**
     * Displays a form where the user can enter their email to reset the password on the associated account
     */
    @GetMapping("/resetPassword")
    public String showResetPasswordStarterForm(Model model) {
        model.addAttribute("user_email", new UserEmailDto());
        return "reset-password-starter-form";
    }

    /**
     * Handles processing of the password reset form submission. If there are no errors, sends the email to the user
     * to allow them to reset their password. If the email is valid and belongs to an enabled user, sends that user
     * a password reset email. If the email is validly formed but not associated with an enabled user, do not send
     * an email and simply redirect them to the home page without any errors (for security purposes).
     */
    @PostMapping("/processResetPasswordStarterForm")
    public ModelAndView processResetPasswordStarterForm(@Valid @ModelAttribute("user_email") UserEmailDto userEmailDto,
                                                        BindingResult bindingResult, HttpServletRequest request) {
        ModelAndView mv;

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("reset-password-starter-form", "user_email", userEmailDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

        User user = userService.getUserByEmail(userEmailDto.getEmail());

        if (user != null && user.isEnabled()) {
            PasswordResetToken resetToken = userService.createPasswordResetTokenForUser(user);
            sendPasswordResetEmail(getAppUrl(request), request.getLocale(), resetToken, user);
        }

        mv = new ModelAndView("redirect:/");
        return mv;
    }

    /**
     * Displays the form for users to enter a new password after a password reset request email link is clicked.
     * If the user and token are valid, it displays the form, otherwise, it displays an error and redirects to /login.
     */
    @GetMapping("/changePassword")
    public String showChangePasswordForm(Model model, @RequestParam("username") String username,
                                         @RequestParam("token") String token) {
        User user = userService.getUser(username);
        boolean isValidResetToken = userService.validatePasswordResetToken(user, token);
        if (user == null || !user.isEnabled() || !isValidResetToken) {
            if (user != null) {
            }
            return "redirect:/login?passwordResetError";
        }

        UserPasswordDto userPasswordDto = new UserPasswordDto();
        model.addAttribute("userPasswordDto", userPasswordDto);
        return "change-password-form";
    }

    /**
     * Handles processing the password change when the password change form is submitted. If there are input errors or
     * and invalid user or token, display those errors. Otherwise, resets the user's password and redirects to /login
     * and inform the user of the success of the password reset.
     */
    @PostMapping("/processChangePassword")
    public ModelAndView processChangePasswordForm(@Valid UserPasswordDto userPasswordDto, BindingResult bindingResult) {
        ModelAndView mv;

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("change-password-form", "userPasswordDto", userPasswordDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

        User user = userService.getUser(userPasswordDto.getUsername());
        boolean isValidResetToken = userService.validatePasswordResetToken(user, userPasswordDto.getToken());

        if (!isValidResetToken || !user.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE)) {
            mv = new ModelAndView("redirect:/login?passwordResetError");
            return mv;
        }

        userService.changeUserPassword(user, userPasswordDto.getPassword());

        mv = new ModelAndView("redirect:/login?passwordUpdateSuccess");
        return mv;
    }

    /**
     * Displays a page that provides means of searching all users of this application. This page also displays the results
     * of such a search (including errors on invalid searches), and no results (if no users matching the search are found).
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/users")
    public String showUsersListPage(ServletRequest request, Model model, @RequestParam(required = false) String search,
                                    @RequestParam(required = false) String searchError)
            throws UnsupportedEncodingException {

        if (request.getParameterMap().containsKey("search")) {
            SortedSet<String> usernames = userService.searchForUsernames(search);
            model.addAttribute("usernames", usernames);
        }

        model.addAttribute("userSearchDto", new UserSearchDto());
        return "user-search-page";
    }

    /**
     * Handles processing of searches for Users. If the search is invalid, display an error on the /users page, otherwise
     * display the /users page with a list of all users matching the search
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/processSearchUsers")
    public String processesSearchForUsers(@Valid UserSearchDto userSearchDto, BindingResult bindingResult)
            throws UnsupportedEncodingException {

        if (bindingResult.hasErrors()) {
            return "redirect:/users?searchError";
        }

        String searchText = URLEncoderDecoderHelper.encode(userSearchDto.getSearchText().trim());
        return "redirect:/users?search=" + searchText;
    }

    /**
     * Handler method that handles displaying an error page when a UserNotFoundException occurs.
     *
     * @param model the model to add a message to
     * @param ex the exception to handle
     * @return the name of the error page to display
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(Model model, UserNotFoundException ex) {
        model.addAttribute("message", ex.getMessage());
        return "user-not-found";
    }

    /**
     * Helper method that creates the app's url with current context path
     *
     * @param request the request to get the URL from
     * @return the app's url including current context path
     */
    private String getAppUrl(HttpServletRequest request) {
//        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toString() + request.getContextPath();
    }

    /**
     * Helper method that sends an email to the user including a link to allow them to reset their password.
     *
     * @param appUrl the url to click on
     * @param locale the locale
     * @param token the token to send
     * @param user the user to whom the token belongs
     */
    private void sendPasswordResetEmail(String appUrl, Locale locale, PasswordResetToken token, User user) {
        String resetUrl = appUrl + "/changePassword?username=" + user.getUsername() + "&token=" + token.getToken();
        String message = messageSource.getMessage("message.resetPasswordLinkPrompt", null, locale) + "\n" + resetUrl;
        String subject = "Multi-Topic Forum Password Reset";

        emailService.sendEmail(user.getEmail(), subject, message);
    }

    /**
     * Helper method that sends an email to the user including a link with an updated registration verification token.
     *
     * @param appUrl the url to click on
     * @param locale the locale
     * @param newToken the token to send
     * @param user the user attempting to register
     */
    private void sendResendVerificationTokenEmail(String appUrl, Locale locale, VerificationToken newToken, User user) {
        String confirmationUrl = appUrl + "/registrationConfirm?token=" + newToken.getToken();
        String message = messageSource.getMessage("message.resendToken", null, locale) + "\n" + confirmationUrl;
        String subject = "Multi-Topic Forum Registration Confirmation : Re-sent";

        emailService.sendEmail(user.getEmail(), subject, message);
    }

    /**
     * Helper method that updates the BindingResult with errors indicating if the username or email is already in use
     *
     * @param userDto the userDto to check for duplicate information
     * @param bindingResult the binding result to update
     * @return the updated binding result
     */
    private BindingResult updateRegistrationBindingResult(UserDto userDto, BindingResult bindingResult) {
        if (userService.usernameExists(userDto.getUsername())) {
            bindingResult.rejectValue("username", "message.regError",
                    "Username " + userDto.getUsername() + " is taken");
        }

        if (userService.emailExists(userDto.getEmail())) {
            bindingResult.rejectValue("email", "message.regError",
                    "Email " + userDto.getEmail() + " already exists");
        }

        return bindingResult;
    }
}
