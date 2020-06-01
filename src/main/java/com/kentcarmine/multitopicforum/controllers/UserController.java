package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.dtos.UserSearchDto;
import com.kentcarmine.multitopicforum.exceptions.PageNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.DisciplineService;
import com.kentcarmine.multitopicforum.services.TopicThreadService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

/**
 * Controller for handling all user-related tasks (ie. login/logout, display of a user's information, lists of users,
 * etc)
 */
@Controller
public class UserController {

    @Value("${spring.data.web.pageable.default-page-size}")
    private int POSTS_PER_PAGE;

    private final UserService userService;
    private final DisciplineService disciplineService;
    private final TopicThreadService topicThreadService;

    @Autowired
    public UserController(UserService userService, DisciplineService disciplineService, TopicThreadService topicThreadService) {
        this.userService = userService;
        this.disciplineService = disciplineService;
        this.topicThreadService = topicThreadService;
    }

    /**
     * Show the profile page of the user with the given name, or throw a UserNotFoundException if no such user exists
     */
    @GetMapping("/users/{username}")
    public String showUserPage(Model model, @PathVariable String username, @RequestParam(required = false, defaultValue = "1") int page) {
        if (userService.usernameExists(username)) {
            User user = userService.getUser(username);
            User loggedInUser = userService.getLoggedInUser();
            disciplineService.handleDisciplinedUser(loggedInUser);

            Page<Post> posts = topicThreadService.getPostPageByUser(user, page, POSTS_PER_PAGE);
            if (posts == null) {
                throw new PageNotFoundException();
            }

            model.addAttribute("user", user);
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("posts", posts);

            if (loggedInUser != null) {
                UserRankAdjustmentDto userRankAdjustmentDto = userService.getUserRankAdjustmentDtoForUser(user, loggedInUser);
                model.addAttribute("userRankAdjustmentDto", userRankAdjustmentDto);
            }

            return "user-page";
        } else {
            throw new UserNotFoundException("Exception.user.notfound", username);
        }
    }

    /**
     * Displays a page that provides means of searching all users of this application. This page also displays the results
     * of such a search (including errors on invalid searches), and no results (if no users matching the search are found).
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/users")
    public String showUsersListPage(ServletRequest request, Model model, @RequestParam(required = false) String search,
                                    @RequestParam(required = false) String searchError)
            throws UnsupportedEncodingException {

        if (request.getParameterMap().containsKey("search")) {
//            SortedSet<String> usernames = userService.searchForUsernames(search);
//            model.addAttribute("usernames", usernames);
            model.addAttribute("search", search);
            model.addAttribute("userSearchResults", userService.searchForUsernames(search));
        }

        model.addAttribute("userSearchDto", new UserSearchDto());
        return "user-search-page";
    }

    /**
     * Handles processing of searches for Users. If the search is invalid, display an error on the /users page, otherwise
     * display the /users page with a list of all users matching the search
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/processSearchUsers")
    public String processesSearchForUsers(@Valid UserSearchDto userSearchDto, BindingResult bindingResult)
            throws UnsupportedEncodingException {

        if (bindingResult.hasErrors()) {
            return "redirect:/users?searchError";
        }

        String searchText = URLEncoderDecoderHelper.encode(userSearchDto.getSearchText().trim());
        return "redirect:/users?search=" + searchText;
    }
}
