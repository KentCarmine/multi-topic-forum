package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
        System.out.println("### isValidRestoration: " + isValidRestoration);

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

    /**
     * Handles processing of AJAX submission of a user promotion request.
     */
    @PostMapping(value = "/promoteUserAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processPromoteUser(@RequestBody PromoteUserSubmissionDto promoteUserSubmissionDto) {
        User userToPromote = userService.getUser(promoteUserSubmissionDto.getUsername());
        User loggedInUser = userService.getLoggedInUser();
        UserRole promotableRank = promoteUserSubmissionDto.getPromotableRank();

        if (userToPromote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PromoteUserResponseDto("Error: User not found"));
        }

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PromoteUserResponseDto("Error: Insufficient permissions to promote that user."));
        }

        if (userService.isValidPromotionRequest(loggedInUser, userToPromote, promotableRank)) {
            userToPromote = userService.promoteUser(userToPromote);

            String msg = userToPromote.getUsername() + " promoted to "
                    + userToPromote.getHighestAuthority().getDisplayRank() + ".";
            String newPromoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                    + "/promoteUserButton/" + userToPromote.getUsername();
            String newDemoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                    + "/demoteUserButton/" + userToPromote.getUsername();

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new PromoteUserResponseDto(msg, newPromoteButtonUrl, newDemoteButtonUrl));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PromoteUserResponseDto("Error: Insufficient permissions to promote that user."));
        }
    }

    /**
     * Handles processing of AJAX submission of a user demotion request.
     */
    @PostMapping(value = "/demoteUserAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processDemoteUser(@RequestBody DemoteUserSubmissionDto demoteUserSubmissionDto) {
        System.out.println("### /demoteUserAjax called");
        User userToDemote = userService.getUser(demoteUserSubmissionDto.getUsername());
        User loggedInUser = userService.getLoggedInUser();
        UserRole demotableRank = demoteUserSubmissionDto.getDemotableRank();

        if (userToDemote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DemoteUserResponseDto("Error: User not found"));
        }

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DemoteUserResponseDto("Error: Insufficient permissions to demote that user."));
        }

        if (userService.isValidDemotionRequest(loggedInUser, userToDemote, demotableRank)) {
            System.out.println("### in /demoteUserAjax, valid demotion request");
            userToDemote = userService.demoteUser(userToDemote);
            System.out.println("### Demoted User: " + userToDemote);

            String msg = userToDemote.getUsername() + " demoted to "
                    + userToDemote.getHighestAuthority().getDisplayRank() + ".";
            String newPromoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                    + "/promoteUserButton/" + userToDemote.getUsername();
            String newDemoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                    + "/demoteUserButton/" + userToDemote.getUsername();

            return ResponseEntity.status(HttpStatus.OK).body(new DemoteUserResponseDto(msg, newPromoteButtonUrl,
                    newDemoteButtonUrl));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DemoteUserResponseDto("Error: Insufficient permissions to demote that user."));
        }
    }

    /**
     * Provides a promotion button for a user with the given username in an up-to-date state.
     */
    @GetMapping(value = "/demoteUserButton/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView demoteUserButton(@PathVariable String username) {
        System.out.println("### /demoteUserButton/" + username + " called");
        User loggedInUser = userService.getLoggedInUser();
        User user = userService.getUser(username);

        ModelAndView mv;

        if (user == null || loggedInUser == null) {
            System.out.println("### Invalid use of /demoteUserButton");
            mv = new ModelAndView();
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }

        mv = new ModelAndView("fragments/promote-demote-buttons :: demote-button-fragment");
        mv.setStatus(HttpStatus.OK);
        mv.addObject("user", user);
        mv.addObject("loggedInUser", loggedInUser);
        return mv;
    }

    /**
     * Provides a promotion button for a user with the given username in an up-to-date state.
     */
    @GetMapping(value = "/promoteUserButton/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView promoteUserButton(@PathVariable String username) {
        User loggedInUser = userService.getLoggedInUser();
        User user = userService.getUser(username);

        ModelAndView mv;

        if (user == null || loggedInUser == null) {
            System.out.println("### Invalid use of /promoteUserButton");
            mv = new ModelAndView();
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }

        mv = new ModelAndView("fragments/promote-demote-buttons :: promote-button-fragment");
        mv.setStatus(HttpStatus.OK);
        mv.addObject("user", user);
        mv.addObject("loggedInUser", loggedInUser);
        return mv;
    }
}
