package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller that handles displaying the homepage and other base pages
 */
@Controller
public class HomeController {

    private MessageService messageService;

    private String requestForumName;

    @Autowired
    public HomeController(MessageService messageService) {
        this.requestForumName = messageService.getMessage("com.kentcarmine.multitopicforum.requestTopicForumCreationForum.name");
    }

    @GetMapping("/")
    public String getHomePage(Model model) {
        model.addAttribute("requestForumName", requestForumName);

        return "home";
    }

    @GetMapping("/forbidden")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String getAccessDeniedPage() {
        return "access-denied-page";
    }
}
