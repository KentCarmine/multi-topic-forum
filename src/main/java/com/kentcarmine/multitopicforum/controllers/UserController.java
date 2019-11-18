package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login-form";
    }

    @GetMapping("/users/{username}")
    public String showUserPage(Model model, @PathVariable String username) {
//        model.addAttribute("username", username);
//        System.out.println("#######");
//        System.out.println(userService.getLoggedInUserName());
//        System.out.println("#######");
//        userService.printAuth();
//        System.out.println("#######");

        if (userService.userWithNameExists(username)) {
            User user = userService.getUser(username);
            model.addAttribute("user", user);
            return "user-page";
        } else {
            throw new UserNotFoundException("User with name " + username + " was not found");
        }
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(Model model, UserNotFoundException ex) {
        model.addAttribute("message", ex.getMessage());
        return "user-not-found";
    }


}
