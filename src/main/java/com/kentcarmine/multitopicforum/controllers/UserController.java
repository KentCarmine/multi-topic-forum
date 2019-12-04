package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.events.OnRegistrationCompleteEvent;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            System.out.println("### REDIRECT ###");
            return "redirect:/users/" + loggedInUser.getUsername();
        }

        System.out.println("### NO_REDIRECT ###");

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
    public ModelAndView confirmRegistration(WebRequest request,/* Model model,*/ @RequestParam("token") String token) {
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

        String url = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        SimpleMailMessage email = constructResendVerificationTokenEmail(url, request.getLocale(), newToken, user);
        mailSender.send(email);

        return "redirect:/login?regEmailSent";
    }

    private SimpleMailMessage constructResendVerificationTokenEmail(String contextPath, Locale locale,
                                                                    VerificationToken newToken, User user) {
        String confirmationUrl = contextPath + "/registrationConfirm?token=" + newToken.getToken();
        String message = messageSource.getMessage("message.resendToken", null, locale);
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject("Multi-Topic Forum Registration Confirmation : Re-sent");
        email.setText(message + "\n" + confirmationUrl);
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
