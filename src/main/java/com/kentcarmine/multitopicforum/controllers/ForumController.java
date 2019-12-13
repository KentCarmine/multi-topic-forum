package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.TopicThreadNotFoundException;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * Controller for TopicForum-related actions
 */
@Controller
public class ForumController {

    private final ForumService forumService;
    private final UserService userService;

    @Autowired
    public ForumController(ForumService forumService, UserService userService) {
        this.forumService = forumService;
        this.userService = userService;
    }

    /**
     * Show page with form for creating a new TopicForum. Only accessible to admin and superadmin
     */
    @GetMapping("/createNewForum")
    public String showCreateNewForumPage(Model model) {
        model.addAttribute("topicForumDto", new TopicForumDto());
        return "create-new-forum-page";
    }

    /**
     * Process form for creating a new TopicForum. Only accessible to admin and superadmin. If input is valid, creates
     * the specified forum, otherwise displays errors to user.
     */
    @PostMapping("/processNewForumCreation")
    public ModelAndView processNewForumCreation(@Valid TopicForumDto topicForumDto, BindingResult bindingResult) {
        ModelAndView mv;

        bindingResult = updateForumCreationBindingResult(topicForumDto, bindingResult);

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("create-new-forum-page", "topicForumDto", topicForumDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

        TopicForum createdForum = forumService.createForumByDto(topicForumDto);
        mv = new ModelAndView("redirect:/forum/" + createdForum.getName());
        return mv;
    }

    /**
     * Show the root page of the given forum, if it exists, or an error page, if it doesnt.
     */
    @GetMapping("/forum/{name}")
    public String showForum(Model model, @PathVariable String name) {
        TopicForum forum = forumService.getForumByName(name);

        if (forum == null) {
            throw new ForumNotFoundException("Topic Forum with the name " + name + " was not found.");
        }

        model.addAttribute("forum", forum);
        return "forum-page";
    }

    /**
     * Show page that allows a logged in user to create a new topic thread
     */
    @GetMapping("/forum/{name}/createThread")
    public String showCreateThreadPage(Model model, @PathVariable String name) {
        model.addAttribute("forumName", name);
        model.addAttribute("topicThreadCreationDto", new TopicThreadCreationDto());
        return "create-thread-page";
    }

    /**
     * Handle processing of a form submission to create a new topic thread
     */
    @PostMapping("/forum/{name}/processCreateThread")
    public ModelAndView processCreateThread(@Valid TopicThreadCreationDto topicThreadCreationDto, BindingResult bindingResult, @PathVariable String name) {
        ModelAndView mv;

        if (!forumService.isForumWithNameExists(name)) {
            throw new ForumNotFoundException("Forum " + name + " does not exist");
        }

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("create-thread-page", "topicThreadCreationDto", topicThreadCreationDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

        User currentUser = userService.getLoggedInUser();
        TopicForum forum = forumService.getForumByName(name);
        TopicThread newThread = forumService.createNewTopicThread(topicThreadCreationDto, currentUser, forum);

        mv = new ModelAndView("redirect:/forum/" + name + "/show/" + newThread.getId());
        return mv;
    }

    /**
     * Display a page that shows a given thread and all its posts
     */
    @GetMapping("/forum/{forumName}/show/{threadId}")
    public String showThread(Model model, @PathVariable String forumName, @PathVariable Long threadId) {
        if (!forumService.isForumWithNameExists(forumName)) {
            throw new ForumNotFoundException("Forum " + forumName + " does not exist");
        }

        TopicThread thread = forumService.getThreadByForumNameAndId(forumName, threadId);

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        model.addAttribute("forumName", forumName);
        model.addAttribute("threadTitle", thread.getTitle());
        model.addAttribute("posts", thread.getPosts());
        return "topic-thread-page";
    }

    /**
     * Exception handler that shows an error page when a forum with a given name is not found.
     */
    @ExceptionHandler(ForumNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleForumNotFound(Model model, ForumNotFoundException ex) {
        model.addAttribute("message", ex.getMessage());
        return "forum-not-found";
    }

    /**
     * Exception handler that shows an error page when a forum with a given name is not found.
     */
    @ExceptionHandler(TopicThreadNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleThreadNotFound(Model model, TopicThreadNotFoundException ex) {
        model.addAttribute("message", ex.getMessage());
        return "thread-not-found";
    }

    /**
     * Helper method that adds error data to bindingResult if the topicForumDto describes a forum with a name that
     * already exists
     * @param topicForumDto the TopicForumDto to check for duplicate name
     * @param bindingResult the binding result to update
     * @return the updated binding result
     */
    private BindingResult updateForumCreationBindingResult(TopicForumDto topicForumDto, BindingResult bindingResult) {
        if (forumService.isForumWithNameExists(topicForumDto.getName())) {
            bindingResult.rejectValue("name", "message.forum.creation.duplicateName", "A topic forum with the name " + topicForumDto.getName()
                    + " already exists.");
        }

        return bindingResult;
    }
}
