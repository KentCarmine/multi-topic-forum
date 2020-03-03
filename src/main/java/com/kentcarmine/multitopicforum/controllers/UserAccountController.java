package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.events.OnRegistrationCompleteEvent;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

/**
 * Controller for actions related to a User's account (ie. registration, updating password or tokens, promotions, etc).
 */
@Controller
public class UserAccountController {

    private final UserService userService;
    private final EmailService emailService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserAccountService userAccountService;
    private final DisciplineService disciplineService;
    private final MessageService messageService;

    @Autowired
    public UserAccountController(UserService userService, ApplicationEventPublisher applicationEventPublisher,
                                 EmailService emailService, UserAccountService userAccountService,
                                 DisciplineService disciplineService, MessageService messageService) {
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.emailService = emailService;
        this.userAccountService = userAccountService;
        this.disciplineService = disciplineService;
        this.messageService = messageService;
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
     * Display the form to allow a user to register. If a user is logged in, instead go to that user's profile page
     */
    @GetMapping("/registerUser")
    public String showUserRegistrationForm(Model model) {
        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

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
        disciplineService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser != null) {
            return new ModelAndView("redirect:/users/" + loggedInUser.getUsername());
        }

        updateRegistrationBindingResult(user, bindingResult);

        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("user-registration-form", "user", user);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        } else {
            User registeredUser = userAccountService.createUserByUserDto(user);
            try {
                String appUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString() + request.getContextPath();
                applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(registeredUser, request.getLocale(), appUrl));
            } catch (Exception ex) {
//                System.out.println("### Error occurred completing registration ###");
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
//        System.out.println("### in confirmRegistration()");
        Locale locale = request.getLocale();
        VerificationToken verificationToken = userAccountService.getVerificationToken(token);

        ModelAndView mv;

        if (verificationToken == null) {
//            System.out.println("### in confirmRegistration(), null token case");
            String message = userAccountService.getInvalidAuthTokenMessage(locale);

            mv = new ModelAndView("registration-confirmation-error", HttpStatus.NOT_FOUND);
            mv.getModel().put("message", message);
            return mv;
        }

        User user = verificationToken.getUser();
        if (userAccountService.isVerificationTokenExpired(verificationToken)) {
//            System.out.println("### in confirmRegistration(), isVerificationTokenExpired() case");
            String message = userAccountService.getExpiredAuthTokenMessage(locale);

            mv = new ModelAndView("registration-confirmation-error", HttpStatus.NOT_FOUND);
            mv.getModel().put("message", message);
            mv.getModel().put("expired", true);
            mv.getModel().put("token", token);
            return mv;
        } else if (user != null && user.isEnabled()) {
//            System.out.println("### in confirmRegistration(), already registered case.");
            mv = new ModelAndView("redirect:/login");
            return mv;
        }

//        System.out.println("### in confirmRegistration(), creating registration case");
        userAccountService.saveRegisteredUser(user);
        mv = new ModelAndView("redirect:/login?registrationSuccess");
        return mv;
    }

    /**
     * Resends the registration email to the user upon request (ie. if the previous registration token is expired).
     * If the user already exsists and is enabled, sends them to the login page.
     */
    @GetMapping("/resendRegistrationEmail")
    public String resendRegistrationEmail(HttpServletRequest request, @RequestParam("token") String existingToken) {
        VerificationToken newToken = userAccountService.generateNewVerificationToken(existingToken);

        User user = userAccountService.getUserByVerificationToken(newToken.getToken());

        if (user.isEnabled()) {
            return "redirect:/login";
        }

        emailService.sendResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, user);

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
            PasswordResetToken resetToken = userAccountService.createPasswordResetTokenForUser(user);
            emailService.sendPasswordResetEmail(getAppUrl(request), request.getLocale(), resetToken, user);
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
        boolean isValidResetToken = userAccountService.validatePasswordResetToken(user, token);
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
        boolean isValidResetToken = userAccountService.validatePasswordResetToken(user, userPasswordDto.getToken());

        if (!isValidResetToken || !user.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE)) {
            mv = new ModelAndView("redirect:/login?passwordResetError");
            return mv;
        }

        userAccountService.changeUserPassword(user, userPasswordDto.getPassword());

        mv = new ModelAndView("redirect:/login?passwordUpdateSuccess");
        return mv;
    }

    /**
     * Handles processing of AJAX submission of a user promotion request.
     */
    @PostMapping(value = "/promoteUserAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processPromoteUser(@RequestBody PromoteUserSubmissionDto promoteUserSubmissionDto) {
        User userToPromote = userService.getUser(promoteUserSubmissionDto.getUsername());
        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();
        UserRole promotableRank = promoteUserSubmissionDto.getPromotableRank();

        if (userToPromote == null) {
            String msg = messageService.getMessage("Exception.user.notfound", promoteUserSubmissionDto.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PromoteUserResponseDto(msg));
        }

        if (loggedInUser == null) {
            String msg = messageService.getMessage("Exception.authority.insufficient");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PromoteUserResponseDto(msg));
        }

        if (userService.isValidPromotionRequest(loggedInUser, userToPromote, promotableRank)) {
            userToPromote = userService.promoteUser(userToPromote);
            PromoteUserResponseDto purDto = userService.getPromoteUserResponseDtoForUser(userToPromote);

            return ResponseEntity.status(HttpStatus.OK).body(purDto);
        } else {
            String msg = messageService.getMessage("Exception.authority.insufficient");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PromoteUserResponseDto(msg));
        }
    }

    /**
     * Handles processing of AJAX submission of a user demotion request.
     */
    @PostMapping(value = "/demoteUserAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processDemoteUser(@RequestBody DemoteUserSubmissionDto demoteUserSubmissionDto) {
        User userToDemote = userService.getUser(demoteUserSubmissionDto.getUsername());
        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();
        UserRole demotableRank = demoteUserSubmissionDto.getDemotableRank();

        if (userToDemote == null) {
            String msg = messageService.getMessage("Exception.user.notfound", demoteUserSubmissionDto.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DemoteUserResponseDto(msg));
        }

        if (loggedInUser == null) {
            String msg = messageService.getMessage("Exception.authority.insufficient");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DemoteUserResponseDto(msg));
        }

        if (userService.isValidDemotionRequest(loggedInUser, userToDemote, demotableRank)) {
            userToDemote = userService.demoteUser(userToDemote);
            DemoteUserResponseDto durDto = userService.getDemoteUserResponseDtoForUser(userToDemote);

            return ResponseEntity.status(HttpStatus.OK).body(durDto);
        } else {
            String msg = messageService.getMessage("Exception.authority.insufficient");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DemoteUserResponseDto(msg));
        }
    }

    /**
     * Provides a promotion button for a user with the given username in an up-to-date state.
     */
    @GetMapping(value = "/demoteUserButton/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView demoteUserButton(@PathVariable String username) {
        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();
        User user = userService.getUser(username);

        ModelAndView mv;

        if (user == null || loggedInUser == null) {
            System.out.println("### Invalid use of /demoteUserButton");
            mv = new ModelAndView();
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }

        UserRankAdjustmentDto userRankAdjustmentDto = userService.getUserRankAdjustmentDtoForUser(user, loggedInUser);

        mv = new ModelAndView("fragments/promote-demote-buttons :: demote-button-fragment");
        mv.setStatus(HttpStatus.OK);
        mv.addObject("userRankAdjustmentDto", userRankAdjustmentDto);
        return mv;
    }

    /**
     * Provides a promotion button for a user with the given username in an up-to-date state.
     */
    @GetMapping(value = "/promoteUserButton/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView promoteUserButton(@PathVariable String username) {
        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();
        User user = userService.getUser(username);

        ModelAndView mv;

        if (user == null || loggedInUser == null) {
            System.out.println("### Invalid use of /promoteUserButton");
            mv = new ModelAndView();
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }

        UserRankAdjustmentDto userRankAdjustmentDto = userService.getUserRankAdjustmentDtoForUser(user, loggedInUser);

        mv = new ModelAndView("fragments/promote-demote-buttons :: promote-button-fragment");
        mv.setStatus(HttpStatus.OK);
        mv.addObject("userRankAdjustmentDto", userRankAdjustmentDto);
        return mv;
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

    /**
     * Helper method that creates the app's url with current context path
     *
     * @param request the request to get the URL from
     * @return the app's url including current context path
     */
    private String getAppUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toString() + request.getContextPath();
    }
}
