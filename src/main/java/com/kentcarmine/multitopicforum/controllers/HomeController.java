package com.kentcarmine.multitopicforum.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller that handles displaying the homepage and other base pages
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String getHomePage() {
        return "home";
    }

    @GetMapping("/forbidden")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String getAccessDeniedPage() {
        return "access-denied-page";
    }
}
