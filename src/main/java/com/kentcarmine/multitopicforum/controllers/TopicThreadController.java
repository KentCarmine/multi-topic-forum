package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadSearchDto;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.TopicThreadNotFoundException;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

@Controller
public class TopicThreadController {

    private final ForumService forumService;
    private final UserService userService;
    private final TopicThreadService topicThreadService;
    private final PostVoteService postVoteService;
    private final DisciplineService disciplineService;

    @Autowired
    public TopicThreadController(ForumService forumService, UserService userService,
                                 TopicThreadService topicThreadService, PostVoteService postVoteService,
                                 DisciplineService disciplineService) {
        this.forumService = forumService;
        this.userService = userService;
        this.topicThreadService = topicThreadService;
        this.postVoteService = postVoteService;
        this.disciplineService = disciplineService;
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

            SortedSet<TopicThread> threads = topicThreadService.searchTopicThreads(name, search);

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
        disciplineService.handleDisciplinedUser(loggedInUser);

        TopicForum forum = forumService.getForumByName(name);
//        TopicThread newThread = forumService.createNewTopicThread(topicThreadCreationDto, loggedInUser, forum);
        TopicThread newThread = topicThreadService.createNewTopicThread(topicThreadCreationDto, loggedInUser, forum);

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

        TopicThread thread = topicThreadService.getThreadByForumNameAndId(forumName, threadId);

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        model.addAttribute("forumName", forumName);
        model.addAttribute("threadTitle", thread.getTitle());
        model.addAttribute("threadId", threadId);
        model.addAttribute("threadIsLocked", thread.isLocked());
        model.addAttribute("posts", thread.getPosts());

        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser != null) {
            model.addAttribute("postCreationDto", new PostCreationDto());
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("voteMap", postVoteService.generateVoteMap(loggedInUser, thread));
            model.addAttribute("canLock", topicThreadService.canUserLockThread(loggedInUser, thread));
            model.addAttribute("canUnlock", topicThreadService.canUserUnlockThread(loggedInUser, thread));
        }

        return "topic-thread-page";
    }

    /**
     * Handles processing of a request to lock the thread with the given ID
     */
    @PostMapping("/lockTopicThread/{threadId}")
    public String processLockThread(@PathVariable Long threadId) {
        TopicThread thread = topicThreadService.getThreadById(threadId);
        TopicForum forum = thread.getForum();

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser == null) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?lockThreadError";
        }

        if (thread.isLocked()) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadLocked";
        }

        boolean isLockSuccessful = topicThreadService.lockThread(loggedInUser, thread);

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
        TopicThread thread = topicThreadService.getThreadById(threadId);
        TopicForum forum = thread.getForum();

        if (thread == null) {
            throw new TopicThreadNotFoundException("Thread was not found");
        }

        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        if (loggedInUser == null) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?unlockThreadError";
        }

        if (!thread.isLocked()) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadUnlocked";
        }

        boolean isUnlockSuccessful = topicThreadService.unlockThread(loggedInUser, thread);

        if (isUnlockSuccessful) {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?threadUnlocked";
        } else {
            return "redirect:/forum/" + forum.getName() + "/show/" + threadId + "?unlockThreadError";
        }
    }

}
