package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.events.OnRegistrationCompleteEvent;
import com.kentcarmine.multitopicforum.exceptions.DisciplineNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.InsufficientAuthorityException;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.EmailService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

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
    public UserController(UserService userService, ApplicationEventPublisher applicationEventPublisher,
                          MessageSource messageSource, EmailService emailService) {
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
            User loggedInUser = userService.getLoggedInUser();
            userService.handleDisciplinedUser(loggedInUser);

            model.addAttribute("user", user);
            model.addAttribute("loggedInUser", loggedInUser);
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
        userService.handleDisciplinedUser(loggedInUser);

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
        userService.handleDisciplinedUser(loggedInUser);

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
     * Displays the page for managing disciplinary action against the user with the given name. Allows users with
     * sufficient permissions to ban/suspend that user or reverse active bans or suspensions. Also displays the user's
     * disciplinary history
     */
    @GetMapping("/manageUserDiscipline/{username}")
    public String showManageUserDisciplinePage(@PathVariable String username, Model model) {
        System.out.println("### in showManageUserDisciplinePage()");

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        User user = userService.getUser(username);

        if (user == null) {
            throw new UserNotFoundException("User " + username + " was not found.");
        }

        if (!loggedInUser.isHigherAuthority(user)) {
            throw new InsufficientAuthorityException(loggedInUser.getUsername() + " has insufficient authority to view "
                    + user.getUsername() + "'s disciplines.");
        }

        SortedSet<DisciplineViewDto> activeDisciplines = userService.getActiveDisciplinesForUser(user, loggedInUser);
        SortedSet<DisciplineViewDto> inactiveDisciplines = userService.getInactiveDisciplinesForUser(user);
        UserDisciplineSubmissionDto userDisciplineSubmissionDto = new UserDisciplineSubmissionDto();
        userDisciplineSubmissionDto.setDisciplinedUsername(username);

        System.out.println("### in showManageUserDisciplinePage(). activeDisciplines = " + activeDisciplines);

        model.addAttribute("userDisciplineSubmissionDto", userDisciplineSubmissionDto);
        model.addAttribute("activeDisciplines", activeDisciplines);
        model.addAttribute("inactiveDisciplines", inactiveDisciplines);

        return "user-discipline-page";
    }

    /**
     * Handles processing of a user discipline submission.
     */
    @PostMapping("/processCreateUserDiscipline")
    public ModelAndView processUserDisciplineSubmission(@Valid UserDisciplineSubmissionDto userDisciplineSubmissionDto, BindingResult bindingResult) {
        ModelAndView mv;
//        System.out.println("### in processUserDisciplineSubmission(). UserDisciplineSubmissionDto = " + userDisciplineSubmissionDto.toString());

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        updateDisciplineSubmissionBindingResult(userDisciplineSubmissionDto, bindingResult);

        System.out.println("### in processUserDisciplineSubmission(). DTO = " + userDisciplineSubmissionDto);

        User disciplinedUser = userService.getUser(userDisciplineSubmissionDto.getDisciplinedUsername());

        if (disciplinedUser == null) {
            throw new UserNotFoundException();
        }

        if (bindingResult.hasErrors()) {
            System.out.println("### in processUserDisciplineSubmission() hasErrors case");

            SortedSet<DisciplineViewDto> activeDisciplines = userService.getActiveDisciplinesForUser(disciplinedUser, loggedInUser);
            SortedSet<DisciplineViewDto> inactiveDisciplines = userService.getInactiveDisciplinesForUser(disciplinedUser);

            mv = new ModelAndView("user-discipline-page", "userDisciplineSubmissionDto", userDisciplineSubmissionDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);

            mv.addObject("activeDisciplines", activeDisciplines);
            mv.addObject("inactiveDisciplines", inactiveDisciplines);
            return mv;
        }

        System.out.println("### disciplinedUser.getDisciplines() = " + disciplinedUser.getDisciplines());

        boolean successfulBan = userService.disciplineUser(userDisciplineSubmissionDto, loggedInUser);

        System.out.println("### successfulBan = " + successfulBan);

        String url = "redirect:/users/" + disciplinedUser.getUsername();

        if (successfulBan) {
            url = url + "?userDisciplined";
        } else {
            url = url + "?userAlreadyBanned";
        }

        mv = new ModelAndView(url);
        return mv;
    }

    /**
     * Shows the discipline status page for the given user, informing them of their disciplinary status (if there is one
     * active), or redirecting them to their home page if there is not. If there is a disciplinary status, also forcibly
     * logs the user out and clears their remember me cookie.
     */
    @GetMapping("/showDisciplineInfo/{username}")
    public String showDisciplineInfoPage(@PathVariable String username, Model model, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getUser(username);
        User loggedInUser = userService.getLoggedInUser();

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!user.equals(loggedInUser)) {
            return "redirect:/showDisciplineInfo/" + loggedInUser.getUsername();
        }

        if (!loggedInUser.isBannedOrSuspended()) {
            return "redirect:/login";
        }

        Discipline greatestDurationActiveDiscipline = loggedInUser.getGreatestDurationActiveDiscipline();

        StringBuilder msgBuilder = new StringBuilder("You have been ");

        if (greatestDurationActiveDiscipline.isBan()) {
            msgBuilder.append("permanently banned.");
        } else {
            String endsAtStr = greatestDurationActiveDiscipline.getDisciplineEndTime().toString();
            msgBuilder.append("suspended. Your suspension will end at: " + endsAtStr + ".");
        }

        msgBuilder.append(" The reason given for this disciplinary action was: " + greatestDurationActiveDiscipline.getReason());

        model.addAttribute("username", username);
        model.addAttribute("message", msgBuilder.toString());

        userService.forceLogOut(loggedInUser, request, response);

        return "user-discipline-info-page";
    }

    /**
     * Handles rescinding a discipline with the given id and associated with the user with the given username, provided
     * the logged in user has the authority to do so.
     */
    @PostMapping("/rescindDiscipline/{username}/{id}")
    public String processRescindDiscipline(@PathVariable String username, @PathVariable Long id) {
        System.out.println("### in processRescindDiscipline. username = " + username + ", id = " + id);

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        User disciplinedUser = userService.getUser(username);
        if (disciplinedUser == null) {
            System.out.println("Error in processRescindDiscipline. disciplinedUser is null");
            throw new UserNotFoundException("User not found");
        }

        Discipline disciplineToRescind = userService.getDisciplineByIdAndUser(id, disciplinedUser);
        if (disciplineToRescind == null) {
            System.out.println("Error in processRescindDiscipline. disciplineToRescind is null");
            throw new DisciplineNotFoundException("Discipline to rescind was not found");
        }

        if (!loggedInUser.equals(disciplineToRescind.getDiscipliningUser()) && !loggedInUser.isHigherAuthority(disciplineToRescind.getDiscipliningUser())) {
            throw new InsufficientAuthorityException("Insufficient authority to rescind discipline");
        }

        userService.rescindDiscipline(disciplineToRescind);

        return "redirect:/manageUserDiscipline/" + disciplinedUser.getUsername();
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

    private BindingResult updateDisciplineSubmissionBindingResult(UserDisciplineSubmissionDto userDisciplineSubmissionDto, BindingResult bindingResult) {
        User loggedInUser = userService.getLoggedInUser();
        User disciplinedUser = userService.getUser(userDisciplineSubmissionDto.getDisciplinedUsername());

        if (disciplinedUser == null) {
            System.out.println("### in updateDisciplineSubmissionBindingResult(). disciplinedUser == null");
            bindingResult.rejectValue("disciplinedUsername", null, "Could not find user " + userDisciplineSubmissionDto.getDisciplinedUsername());
        }

        if (loggedInUser == null || disciplinedUser == null || !loggedInUser.isHigherAuthority(disciplinedUser)) {
            bindingResult.reject("insufficientAuthority", null, "You do not have the authority to discipline this user");
        }

        return bindingResult;
    }
}
