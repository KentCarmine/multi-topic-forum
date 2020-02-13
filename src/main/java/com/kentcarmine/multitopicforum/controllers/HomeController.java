package com.kentcarmine.multitopicforum.controllers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller that handles displaying the homepage and other base pages
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String getHomePage() {
//        System.out.println("### in getHomePage. Auth = " + SecurityContextHolder.getContext().getAuthentication().toString());
        return "home";
    }

    @GetMapping("/forbidden")
    public String getAccessDeniedPage() {
        return "access-denied-page";
    }
}
