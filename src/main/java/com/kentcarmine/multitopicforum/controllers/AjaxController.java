package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.PostVote;
import com.kentcarmine.multitopicforum.model.PostVoteState;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.sql.Date;
import java.time.Instant;

/**
 * Handles AJAX submission of and response to upvotes and downvotes on posts.
 */
@RestController
public class AjaxController {

    private final ForumService forumService;
    private final UserService userService;

    @Autowired
    public AjaxController(ForumService forumService, UserService userService) {
        this.forumService = forumService;
        this.userService = userService;
    }

    /**
     * Handles processing of AJAX submission of an upvote or downvote on a post.
     */
    @PostMapping(value = "/handleVoteAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostVoteResponseDto> processVoteSubmission(@Valid @RequestBody PostVoteSubmissionDto postVoteSubmissionDto, Errors errors) {
        PostVoteResponseDto response;

//        System.out.println("### PostVoteSub = " + postVoteSubmissionDto.toString());

        User loggedInUser = userService.getLoggedInUser();
        Post post = forumService.getPostById(postVoteSubmissionDto.getPostId());

        if (loggedInUser == null || post == null || errors.hasErrors()) {
            System.out.println("### Invalid vote submission: " + loggedInUser + ", " + post + ", " + errors.toString());
            response = new PostVoteResponseDto(null,false ,false, false, 0);
            return ResponseEntity.unprocessableEntity().body(response);
        }

        PostVote postVote = forumService.getPostVoteByUserAndPost(loggedInUser, post);
        if (postVote == null || postVote.getPostVoteState().equals(PostVoteState.NONE)) {
//            System.out.println("### Valid vote submission: " + postVote);
            response = forumService.handlePostVoteSubmission(loggedInUser, post, postVoteSubmissionDto);
//            System.out.println("### Response: " + response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            System.out.println("### Invalid vote submission 2: " + loggedInUser + ", " + post);
            response = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), false, post.getVoteCount());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Handles processing of AJAX submission of a delete request on a post.
     */
    @PostMapping(value = "/deletePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletePostResponseDto> processDeletePost(@RequestBody DeletePostSubmissionDto deletePostSubmissionDto) {
//        System.out.println("DeletePostSubmissionDto = " + deletePostSubmissionDto);
        Post postToDelete = forumService.getPostById(deletePostSubmissionDto.getPostId());
//        System.out.println("PostToDelete = " + postToDelete.toString());

        if (postToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DeletePostResponseDto("Error: Post not found.", null));
        }

        User postingUser = postToDelete.getUser();
//        System.out.println("### LoggedInUser: " + userService.getLoggedInUser());
//        System.out.println("### PostingUser: " + postingUser);
        if (!userService.getLoggedInUser().isHigherAuthority(postingUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DeletePostResponseDto("Error: Insufficient permissions to delete that post.", postToDelete.getId()));
        }

        String postUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/forum/" + postToDelete.getThread().getForum().getName()
                + "/show/" + postToDelete.getThread().getId()
                + "#post_id_" + postToDelete.getId();

        if (!postToDelete.isDeleted()) {
            postToDelete = forumService.deletePost(postToDelete, userService.getLoggedInUser());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new DeletePostResponseDto("Post deleted.", postToDelete.getId(), postUrl));
    }

    /**
     * Handles processing of AJAX submission of a restore request on a deleted post.
     */
    @PostMapping(value = "/restorePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestorePostResponseDto> processDeletePost(@RequestBody RestorePostSubmissionDto restorePostSubmissionDto) {
        Post postToRestore = forumService.getPostById(restorePostSubmissionDto.getPostId());

        if (postToRestore == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestorePostResponseDto("Error: Post not found.", null));
        }

        boolean isValidRestoration = postToRestore.isRestorableBy(userService.getLoggedInUser());

        if (isValidRestoration) {
            postToRestore = forumService.restorePost(postToRestore);
            String postUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                    + "/forum/" + postToRestore.getThread().getForum().getName()
                    + "/show/" + postToRestore.getThread().getId()
                    + "#post_id_" + postToRestore.getId();
            return ResponseEntity.status(HttpStatus.OK).body(new RestorePostResponseDto("Post restored.", postToRestore.getId(), postUrl));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestorePostResponseDto("Error: Insufficient permissions to restore that post.", postToRestore.getId()));
        }
    }

}
