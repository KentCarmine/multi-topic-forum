package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.PostVote;
import com.kentcarmine.multitopicforum.model.PostVoteState;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class VoteController {

    private final ForumService forumService;
    private final UserService userService;

    @Autowired
    public VoteController(ForumService forumService, UserService userService) {
        this.forumService = forumService;
        this.userService = userService;
    }

    @PostMapping(value = "/handleVoteAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processVoteSubmission(@Valid @RequestBody PostVoteSubmissionDto postVoteSubmissionDto, Errors errors) {
        PostVoteResponseDto response;

        System.out.println("### PostVoteSub = " + postVoteSubmissionDto.toString());

        User loggedInUser = userService.getLoggedInUser();
        Post post = forumService.getPostById(postVoteSubmissionDto.getPostId());

        if (loggedInUser == null || post == null || errors.hasErrors()) {
            System.out.println("### Invalid vote submission: " + loggedInUser + ", " + post + ", " + errors.toString());
            response = new PostVoteResponseDto(null,false ,false, false, post.getVoteCount());
            return ResponseEntity.unprocessableEntity().body(response);
        }

        PostVote postVote = forumService.getPostVoteByUserAndPost(loggedInUser, post);
        if (postVote == null || postVote.getPostVoteState().equals(PostVoteState.NONE)) {
            System.out.println("### Valid vote submission");
            response = forumService.handlePostVoteSubmission(loggedInUser, post, postVoteSubmissionDto);
            return ResponseEntity.ok(response);
        } else {
            System.out.println("### Invalid vote submission 2: " + loggedInUser + ", " + post + ", " + errors.toString());
            response = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), false, post.getVoteCount());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
