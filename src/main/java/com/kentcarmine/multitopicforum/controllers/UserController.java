package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @PreAuthorize("!isAuthenticated()")
    @GetMapping("/login")
    public String showLoginForm() {
//        User loggedInUser = userService.getLoggedInUser();
//        if (loggedInUser != null) {
//            System.out.println("### REDIRECT ###");
//            return "redirect:/users/" + loggedInUser.getUsername();
//        }
//
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

//    @PreAuthorize("!isAuthenticated()")
    @GetMapping("/registerUser")
    public String showUserRegistrationForm(Model model) {
//        User loggedInUser = userService.getLoggedInUser();
//        if (loggedInUser != null) {
//            return "redirect:/users/" + loggedInUser.getUsername();
//        }

        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "user-registration-form";
    }

//    @PreAuthorize("!isAuthenticated()")
    @PostMapping("/processUserRegistration")
    public ModelAndView processUserRegistration(@Valid @ModelAttribute("user") UserDto user, BindingResult bindingResult) {
//        User loggedInUser = userService.getLoggedInUser();
//        if (loggedInUser != null) {
//            return new ModelAndView("redirect:/users/" + loggedInUser.getUsername());
//        }

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
            userService.createUserByUserDto(user);
            return new ModelAndView("redirect:/login?registrationSuccess");
        }
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

//    @ExceptionHandler(AccessDeniedException.class)
//    public String handleUnauthorized(AccessDeniedException ex, HttpServletRequest req, HttpServletResponse res){
////        System.out.println("### START ###");
////        System.out.println("### REQUEST ###");
////        System.out.println(req.toString());
////        System.out.println("### RESPONSE ###");
////        System.out.println(res.toString());
////        System.out.println("### EXCEPTION ###");
////        System.out.println(ex.toString());
////        System.out.println("### END ###");
//        String loggedInUsername = req.getUserPrincipal().getName();
//        System.out.println("### User: " + loggedInUsername);
//
//        return "redirect:/users/" + loggedInUsername;
//    }
}
