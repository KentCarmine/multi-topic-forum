package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.TopicThreadNotFoundException;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.PostService;
import com.kentcarmine.multitopicforum.services.TopicThreadService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class PostController {

    private final ForumService forumService;
    private final UserService userService;
    private final TopicThreadService topicThreadService;
    private final PostService postService;

    @Autowired
    public PostController(ForumService forumService, UserService userService, TopicThreadService topicThreadService, PostService postService) {
        this.forumService = forumService;
        this.userService = userService;
        this.topicThreadService = topicThreadService;
        this.postService = postService;
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

//        TopicThread thread = forumService.getThreadByForumNameAndId(forumName, threadId);
        TopicThread thread = topicThreadService.getThreadByForumNameAndId(forumName, threadId);

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

        postService.addNewPostToThread(postCreationDto, loggedInUser, thread);

        mv = new ModelAndView("redirect:/forum/" + forumName + "/show/" + threadId);
        return mv;
    }

    /**
     * Handles processing of AJAX submission of a delete request on a post.
     */
    @PostMapping(value = "/deletePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletePostResponseDto> processDeletePost(@RequestBody DeletePostSubmissionDto deletePostSubmissionDto) {
        Post postToDelete = postService.getPostById(deletePostSubmissionDto.getPostId());

        if (postToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DeletePostResponseDto("Error: Post not found.", null));
        }

        User postingUser = postToDelete.getUser();
        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();

        if (loggedInUser == null || !loggedInUser.isHigherAuthority(postingUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DeletePostResponseDto("Error: Insufficient permissions to delete that post.", postToDelete.getId()));
        }

        String postUrl = postService.getGetDeletedPostUrl(postToDelete);

        postToDelete = postService.deletePost(postToDelete, loggedInUser);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new DeletePostResponseDto("Post deleted.", postToDelete.getId(), postUrl));
    }

    /**
     * Handles processing of AJAX submission of a restore request on a deleted post.
     */
    @PostMapping(value = "/restorePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestorePostResponseDto> processRestorePost(@RequestBody RestorePostSubmissionDto restorePostSubmissionDto) {
        Post postToRestore = postService.getPostById(restorePostSubmissionDto.getPostId());

        if (postToRestore == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestorePostResponseDto("Error: Post not found.", null));
        }

        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();
        boolean isValidRestoration = postToRestore.isRestorableBy(loggedInUser);

        if (isValidRestoration) {
            postToRestore = postService.restorePost(postToRestore);
            String postUrl = postService.getRestoredPostUrl(postToRestore);

            return ResponseEntity.status(HttpStatus.OK).body(new RestorePostResponseDto("Post restored.", postToRestore.getId(), postUrl));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestorePostResponseDto("Error: Insufficient permissions to restore that post.", postToRestore.getId()));
        }
    }
}
