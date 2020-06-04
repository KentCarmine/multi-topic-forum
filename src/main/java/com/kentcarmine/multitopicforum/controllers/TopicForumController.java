package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumSearchDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadSearchDto;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.PageNotFoundException;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.services.ForumService;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.stream.Collectors;

/**
 * Controller for TopicForum-related actions
 */
@Controller
public class TopicForumController {

    private final ForumService forumService;

    @Autowired
    public TopicForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    /**
     * Display a page that lists all TopicForums, or a page that lists the TopicForums matching the search criteria. If
     * there are no forums that fit the criteria, informs the user. If the search was invalid, displays all TopicForums
     * and informs the user the search was invalid.
     */
    @GetMapping("/forums")
    public String showForumsPage(ServletRequest request, Model model,
                                 @RequestParam(required = false, defaultValue = "1") int page,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String searchError) throws UnsupportedEncodingException {

        Page<TopicForumViewDto> forums;
        if (search == null || search.equals("") || request.getParameterMap().containsKey("searchError")) {
            forums = forumService.getForumsAsViewDtosPaginated(page);
        } else {
            forums = forumService.searchTopicForumsForViewDtosPaginated(search, page);
        }

        if(forums == null) {
            throw new PageNotFoundException();
        }

        model.addAttribute("search", search);
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
            throw new ForumNotFoundException();
        }

        model.addAttribute("topicThreadSearchDto", new TopicThreadSearchDto());
        model.addAttribute("forum", forumService.getTopicForumViewDtoForTopicForum(forum));
        return "forum-page";
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
        return "redirect:/forums" + searchParams;
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
            bindingResult.rejectValue("name", "Forum.creation.duplicateName", "A topic forum with the name " + topicForumDto.getName()
                    + " already exists.");
        }

        return bindingResult;
    }
}
