package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling administrative actions performed by Moderators, Admins, or SuperAdmins.
 */
@Controller
public class AdministrationController {

//    private final UserService userService;
//
//    @Autowired
//    public AdministrationController(UserService userService) {
//        this.userService = userService;
//    }

    @GetMapping("/administration")
    public String showAdministrationPage() {
        return "administration-home-page";
    }
}
