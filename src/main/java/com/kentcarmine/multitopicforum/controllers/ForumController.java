package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.services.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * Controller for TopicForum-related actions
 */
@Controller
public class ForumController {

    private final ForumService forumService;

    @Autowired
    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/createNewForum")
    public String showCreateNewForumPage(Model model) {
        model.addAttribute("topicForumDto", new TopicForumDto());
        return "create-new-forum-page";
    }

    @PostMapping("/processNewForumCreation")
    public ModelAndView processNewForumCreation(@Valid TopicForumDto topicForumDto, BindingResult bindingResult) {
        ModelAndView mv;

//        System.out.println("### Obj: " + topicForumDto.toString());

        bindingResult = updateForumCreationBindingResult(topicForumDto, bindingResult);

        if (bindingResult.hasErrors()) {
            System.out.println("### processNewForumCreation() has Errors");
            for (ObjectError e : bindingResult.getAllErrors()) {
                System.out.println(e.toString());
            }
            mv = new ModelAndView("create-new-forum-page", "topicForumDto", topicForumDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

//        System.out.println("### After errors block");

        TopicForum createdForum = forumService.createForumByDto(topicForumDto);
        mv = new ModelAndView("redirect:/forum/" + createdForum.getName());
        return mv;
    }

    @GetMapping("/forum/{name}")
    public String showForum(Model model, @PathVariable String name) {
        TopicForum forum = forumService.getForumByName(name);

        if (forum == null) {
            throw new ForumNotFoundException("Topic Forum with the name " + name + " was not found.");
        }

        model.addAttribute("forum", forum);
        return "forum-page";
    }

    @ExceptionHandler(ForumNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleForumNotFound(Model model, ForumNotFoundException ex) {
        model.addAttribute("message", ex.getMessage());
        return "forum-not-found";
    }

    private BindingResult updateForumCreationBindingResult(TopicForumDto topicForumDto, BindingResult bindingResult) {
        if (forumService.isForumWithNameExists(topicForumDto.getName())) {
            bindingResult.rejectValue("name", "message.forum.creation.duplicateName", "A topic forum with the name " + topicForumDto.getName()
                    + " already exists.");
        }

        return bindingResult;
    }
}
