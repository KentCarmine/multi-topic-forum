package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.PostVote;
import com.kentcarmine.multitopicforum.model.PostVoteState;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.PostService;
import com.kentcarmine.multitopicforum.services.PostVoteService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
/**
 * Controller that handles requests related to PostVotes.
 */
@RestController
public class PostVoteController {

    private final UserService userService;
    private final PostService postService;
    private final PostVoteService postVoteService;

    @Autowired
    public PostVoteController(UserService userService, PostService postService, PostVoteService postVoteService) {
        this.userService = userService;
        this.postService = postService;
        this.postVoteService = postVoteService;
    }

    /**
     * Handles processing of AJAX submission of an upvote or downvote on a post.
     */
    @PostMapping(value = "/handleVoteAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostVoteResponseDto> processVoteSubmission(@Valid @RequestBody PostVoteSubmissionDto postVoteSubmissionDto, Errors errors) {
        PostVoteResponseDto response;

        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();

        Post post = postService.getPostById(postVoteSubmissionDto.getPostId());

        if (loggedInUser == null || post == null || errors.hasErrors()) {
//            System.out.println("### In processVoteSubmission(). Error case 1");
            response = new PostVoteResponseDto(null,false ,false, false, 0);
            return ResponseEntity.unprocessableEntity().body(response);
        }

        PostVote postVote = postVoteService.getPostVoteByUserAndPost(loggedInUser, post);
        if (postVote == null || postVote.getPostVoteState().equals(PostVoteState.NONE)) {
//            System.out.println("### In processVoteSubmission(). Creation case");
            response = postVoteService.handlePostVoteSubmission(loggedInUser, post, postVoteSubmissionDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
//            System.out.println("### In processVoteSubmission(). Error case 2");
            response = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), false, post.getVoteCount());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
