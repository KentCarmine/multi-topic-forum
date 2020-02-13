package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.TopicThreadNotFoundException;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
     * Display a page that lists all TopicForums, or a page that lists the TopicForums matching the search criteria. If
     * there are no forums that fit the criteria, informs the user. If the search was invalid, displays all TopicForums
     * and informs the user the search was invalid.
     */
    @GetMapping("/forums")
    public String showForumsPage(ServletRequest request, Model model, @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String searchError) throws UnsupportedEncodingException {
        SortedSet<TopicForum> forums;

        if (search == null || search.equals("") || request.getParameterMap().containsKey("searchError")) {
            forums = forumService.getAllForums();
        } else {
            forums = forumService.searchTopicForums(search);
        }

        model.addAttribute("forums", forums);
        model.addAttribute("topicForumSearchDto", new TopicForumSearchDto());
        return "forums-list-page";
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
    public ModelAndView processNewForumCreation(@Valid @ModelAttribute TopicForumDto topicForumDto, BindingResult bindingResult) {
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

        model.addAttribute("topicThreadSearchDto", new TopicThreadSearchDto());
        model.addAttribute("forum", forum);
        return "forum-page";
    }

    /**
     * Handles processing of searches for threads on a forum with a given name.
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/processSearchThreads/{name}")
    public String processSearchThreads(@Valid TopicThreadSearchDto topicThreadSearchDto, BindingResult bindingResult,
                                       @PathVariable String name) throws UnsupportedEncodingException {

        if (!forumService.isForumWithNameExists(name)) {
            throw new ForumNotFoundException("Topic Forum with the name " + name + " was not found.");
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/searchForumThreads/" + name + "?searchError";
        }

        StringBuilder searchUrl = new StringBuilder("?search=");
        searchUrl.append(URLEncoderDecoderHelper.encode(topicThreadSearchDto.getSearchText().trim()));

        return "redirect:/searchForumThreads/" + name + searchUrl.toString();
    }

    /**
     * Displays the list of results of a search for forum threads within a TopicForum with a given name. Displays an
     * error if the search was invalid, and informs the user if no results matching the search were found.
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/searchForumThreads/{name}")
    public String searchForumThreads(ServletRequest request, Model model, @PathVariable String name,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(required = false) String searchError)
            throws UnsupportedEncodingException {

        if (!forumService.isForumWithNameExists(name)) {
            throw new ForumNotFoundException("Topic Forum with the name " + name + " was not found.");
        }

        if (request.getParameterMap().containsKey("search")) {
            String searchText = URLEncoderDecoderHelper.decode(search);

            SortedSet<TopicThread> threads = forumService.searchTopicThreads(name, search);

            model.addAttribute("threads", threads);
            model.addAttribute("forumName", name);
            model.addAttribute("searchText", searchText);
        }

        return "search-threads-results-page";
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
    public ModelAndView processCreateThread(@Valid @ModelAttribute TopicThreadCreationDto topicThreadCreationDto, BindingResult bindingResult, @PathVariable String name) {
        ModelAndView mv;

        if (!forumService.isForumWithNameExists(name)) {
            throw new ForumNotFoundException("Forum " + name + " does not exist");
        }

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("create-thread-page", "topicThreadCreationDto", topicThreadCreationDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        TopicForum forum = forumService.getForumByName(name);
        TopicThread newThread = forumService.createNewTopicThread(topicThreadCreationDto, loggedInUser, forum);

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
        model.addAttribute("threadId", threadId);
        model.addAttribute("threadIsLocked", thread.isLocked());
        model.addAttribute("posts", thread.getPosts());

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser != null) {
            model.addAttribute("postCreationDto", new PostCreationDto());
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("voteMap", forumService.generateVoteMap(loggedInUser, thread));
            model.addAttribute("canLock", forumService.canUserLockThread(loggedInUser, thread));
            model.addAttribute("canUnlock", forumService.canUserUnlockThread(loggedInUser, thread));
        }

        return "topic-thread-page";
    }


    /**
     * Handles processing of a request to lock the thread with the given ID
     */
    @PostMapping("/lockTopicThread/{threadId}")
    public String processLockThread(@PathVariable Long threadId) {
        TopicThread thread = forumService.getThreadById(threadId);
        TopicForum forum = thread.getForum();

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser == null) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?lockThreadError";
        }

        if (thread.isLocked()) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadLocked";
        }

        boolean isLockSuccessful = forumService.lockThread(loggedInUser, thread);

        if (isLockSuccessful) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadLocked";
        } else {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?lockThreadError";
        }
    }

    /**
     * Handles processing of a request to unlock the thread with the given ID
     */
    @PostMapping("/unlockTopicThread/{threadId}")
    public String processUnlockThread(@PathVariable Long threadId) {
        TopicThread thread = forumService.getThreadById(threadId);
        TopicForum forum = thread.getForum();

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser == null) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?unlockThreadError";
        }

        if (!thread.isLocked()) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadUnlocked";
        }

        boolean isUnlockSuccessful = forumService.unlockThread(loggedInUser, thread);

        if (isUnlockSuccessful) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadUnlocked";
        } else {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?unlockThreadError";
        }
    }

    /**
     * Handle processing of form submission for adding a new post to the current thread
     */
    @PostMapping("/forum/{forumName}/show/{threadId}/createPost")
    public ModelAndView addPostToThread(@Valid @ModelAttribute PostCreationDto postCreationDto, BindingResult bindingResult, @PathVariable String forumName,
                                        @PathVariable Long threadId) {
        ModelAndView mv;

        if (!forumService.isForumWithNameExists(forumName)) {
            throw new ForumNotFoundException("Forum " + forumName + " does not exist");
        }

        TopicThread thread = forumService.getThreadByForumNameAndId(forumName, threadId);

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        if (bindingResult.hasErrors()) {
            mv = new ModelAndView("topic-thread-page", "postCreationDto", postCreationDto);
            mv.addObject("forumName", forumName);
            mv.addObject("threadTitle", thread.getTitle());
            mv.addObject("threadId", threadId);
            mv.addObject("posts", thread.getPosts());
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }
        User loggedInUser = userService.getLoggedInUser();
        userService.handleDisciplinedUser(loggedInUser);

        forumService.addNewPostToThread(postCreationDto, loggedInUser, thread);

        mv = new ModelAndView("redirect:/forum/" + forumName + "/show/" + threadId);
        return mv;
    }

    /**
     * Handles processing of submission of TopicForum search form.
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/searchTopicForums")
    public String processTopicForumSearch(@Valid TopicForumSearchDto topicForumSearchDto, BindingResult bindingResult)
            throws UnsupportedEncodingException {

        if (bindingResult.hasErrors()) {
            return "redirect:/forums?searchError";
        }

        String searchParams = "?search=" + URLEncoderDecoderHelper.encode(topicForumSearchDto.getSearchText().trim());
//        System.out.println("#### URL: " + searchParams);
        return "redirect:/forums" + searchParams;
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
