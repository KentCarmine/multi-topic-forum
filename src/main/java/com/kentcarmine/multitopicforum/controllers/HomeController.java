package com.kentcarmine.multitopicforum.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller that handles displaying the homepage
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String getHomePage() {
        return "home";
    }

    @GetMapping("/forbidden")
    public String getAccessDeniedPage() {
        return "access-denied-page";
    }
}
