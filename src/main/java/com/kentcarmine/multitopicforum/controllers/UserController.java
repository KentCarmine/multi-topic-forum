package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.dtos.UserEmailDto;
import com.kentcarmine.multitopicforum.dtos.UserPasswordDto;
import com.kentcarmine.multitopicforum.events.OnRegistrationCompleteEvent;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageSource messageSource;
    private final JavaMailSender mailSender;

    @Autowired
    public UserController(UserService userService, ApplicationEventPublisher applicationEventPublisher, MessageSource messageSource, JavaMailSender mailSender) {
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.messageSource = messageSource;
        this.mailSender = mailSender;
    }

    @GetMapping("/login")
    public String showLoginForm() {
//        SecurityContextHolder.getContext().getAuthentication().getAuthorities().forEach((a) -> {System.out.println(a.toString());}); // TODO: For testing only
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
//            System.out.println("### REDIRECT ###");
            return "redirect:/users/" + loggedInUser.getUsername();
        }

//        System.out.println("### NO_REDIRECT ###");

        return "login-form";
    }

    @GetMapping("/users/{username}")
    public String showUserPage(Model model, @PathVariable String username) {
        if (userService.usernameExists(username)) {
            User user = userService.getUser(username);
            model.addAttribute("user", user);
            return "user-page";
        } else {
            throw new UserNotFoundException("User with name " + username + " was not found");
        }
    }

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

    @PostMapping("/processUserRegistration")
    public ModelAndView processUserRegistration(@Valid @ModelAttribute("user") UserDto user, BindingResult bindingResult, WebRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            return new ModelAndView("redirect:/users/" + loggedInUser.getUsername());
        }

        updateRegistrationBindingResult(user, bindingResult);

        if (bindingResult.hasErrors()) {
//            System.out.println("###ERRORS###");
//            bindingResult.getAllErrors().forEach(objectError -> {
//                System.out.println(objectError.toString());
//            });
//            System.out.println("###END-ERRORS###");
            ModelAndView mv = new ModelAndView("user-registration-form", "user", user);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
//            return new ModelAndView("user-registration-form", "user", user);
            return mv;
        } else {
            User registeredUser = userService.createUserByUserDto(user);
            try {
                String appUrl = request.getContextPath();
                applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(registeredUser, request.getLocale(), appUrl));
            } catch (Exception ex) {
                System.out.println("### Error occurred completing registration ###");
                ex.printStackTrace();
                return new ModelAndView("registration-email-error");
            }

            return new ModelAndView("redirect:/login?regEmailSent");
        }
    }

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

    @GetMapping("/resendRegistrationEmail")
    public String resendRegistrationEmail(HttpServletRequest request, @RequestParam("token") String existingToken) {
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);

        User user = userService.getUserByVerificationToken(newToken.getToken());

        if (user.isEnabled()) {
            return "redirect:/login";
        }

        SimpleMailMessage email = constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, user);
        mailSender.send(email);

        return "redirect:/login?regEmailSent";
    }

    @GetMapping("/resetPassword")
    public String showResetPasswordStarterForm(Model model) {
        model.addAttribute("user_email", new UserEmailDto());
        return "reset-password-starter-form";
    }

    @PostMapping("/processResetPasswordStarterForm")
    public ModelAndView processResetPasswordStarterForm(@Valid @ModelAttribute("user_email") UserEmailDto userEmailDto,
                                                        BindingResult bindingResult, HttpServletRequest request) {
        ModelAndView mv;

        if (bindingResult.hasErrors()) {
//            System.out.println("errors found");
            mv = new ModelAndView("reset-password-starter-form", "user_email", userEmailDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

        User user = userService.getUserByEmail(userEmailDto.getEmail());

        if (user != null && user.isEnabled()) {
//            System.out.println("user found");

            PasswordResetToken resetToken = userService.createPasswordResetTokenForUser(user);
            mailSender.send(constructPasswordResetEmail(getAppUrl(request), request.getLocale(), resetToken, user));
        }

        mv = new ModelAndView("redirect:/");
        return mv;
    }

    @GetMapping("/changePassword")
    public String showChangePasswordForm(Model model, @RequestParam("username") String username,
                                         @RequestParam("token") String token) {
        User user = userService.getUser(username);
        boolean isValidResetToken = userService.validatePasswordResetToken(user, token);
//        System.out.println("### User has change password auth: " + user.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
        if (user == null || !user.isEnabled() || !isValidResetToken) {
//            System.out.println("### User null:" + user == null);
            if (user != null) {
//                System.out.println("### User enabled:" + user.isEnabled());
            }
//            System.out.println("### Valid token: " + isValidResetToken);
            return "redirect:/login?passwordResetError";
        }

        UserPasswordDto userPasswordDto = new UserPasswordDto();
        model.addAttribute("userPasswordDto", userPasswordDto);
        return "change-password-form";
    }

    @PostMapping("/processChangePassword")
    public ModelAndView processChangePasswordForm(@Valid UserPasswordDto userPasswordDto, BindingResult bindingResult) {
        ModelAndView mv;

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("change-password-form", "userPasswordDto", userPasswordDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

//        System.out.println("### In /processChangePassword, username = " + userPasswordDto.getUsername());
//        System.out.println("### In /processChangePassword, token = " + userPasswordDto.getToken());
        User user = userService.getUser(userPasswordDto.getUsername());
        boolean isValidResetToken = userService.validatePasswordResetToken(user, userPasswordDto.getToken());

        if (!isValidResetToken) {
//            System.out.println("### In /processChangePassword, invalid token case");
            mv = new ModelAndView("redirect:/login?passwordResetError");
            return mv;
        }

        userService.changeUserPassword(user, userPasswordDto.getPassword());

        mv = new ModelAndView("redirect:/login?passwordUpdateSuccess");
        return mv;
    }

    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    private SimpleMailMessage constructPasswordResetEmail(String appUrl, Locale locale, PasswordResetToken token, User user) {
        String resetUrl = appUrl + "/changePassword?username=" + user.getUsername() + "&token=" + token.getToken();
        String message = messageSource.getMessage("message.resetPasswordLinkPrompt", null, locale) + "\n" + resetUrl;
        String subject = "Multi-Topic Forum Password Reset";

        return constructEmail(subject, message, user);
    }

    private SimpleMailMessage constructResendVerificationTokenEmail(String appUrl, Locale locale,
                                                                    VerificationToken newToken, User user) {
        String confirmationUrl = appUrl + "/registrationConfirm?token=" + newToken.getToken();
        String message = messageSource.getMessage("message.resendToken", null, locale) + "\n" + confirmationUrl;
        String subject = "Multi-Topic Forum Registration Confirmation : Re-sent";

        return constructEmail(subject, message , user);
    }

    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        return email;
    }

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

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(Model model, UserNotFoundException ex) {
        model.addAttribute("message", ex.getMessage());
        return "user-not-found";
    }
}
