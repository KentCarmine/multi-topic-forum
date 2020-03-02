package com.kentcarmine.multitopicforum.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling administrative actions performed by Moderators, Admins, or SuperAdmins.
 */
@Controller
public class AdministrationController {

    @GetMapping("/administration")
    public String showAdministrationPage() {
        return "administration-home-page";
    }
}
