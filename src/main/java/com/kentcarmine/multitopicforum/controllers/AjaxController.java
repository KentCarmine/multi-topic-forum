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

    // TODO: Refactor into PostVoteController
    /**
     * Handles processing of AJAX submission of an upvote or downvote on a post.
     */
    @PostMapping(value = "/handleVoteAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostVoteResponseDto> processVoteSubmission(@Valid @RequestBody PostVoteSubmissionDto postVoteSubmissionDto, Errors errors) {
        PostVoteResponseDto response;

        User loggedInUser = getLoggedInUserIfNotDisciplined();

        Post post = forumService.getPostById(postVoteSubmissionDto.getPostId());

        if (loggedInUser == null || post == null || errors.hasErrors()) {
            response = new PostVoteResponseDto(null,false ,false, false, 0);
            return ResponseEntity.unprocessableEntity().body(response);
        }

        PostVote postVote = forumService.getPostVoteByUserAndPost(loggedInUser, post);
        if (postVote == null || postVote.getPostVoteState().equals(PostVoteState.NONE)) {
            response = forumService.handlePostVoteSubmission(loggedInUser, post, postVoteSubmissionDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), false, post.getVoteCount());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // TODO: Refactor into PostController
    /**
     * Handles processing of AJAX submission of a delete request on a post.
     */
    @PostMapping(value = "/deletePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletePostResponseDto> processDeletePost(@RequestBody DeletePostSubmissionDto deletePostSubmissionDto) {
        Post postToDelete = forumService.getPostById(deletePostSubmissionDto.getPostId());

        if (postToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DeletePostResponseDto("Error: Post not found.", null));
        }

        User postingUser = postToDelete.getUser();
        User loggedInUser = getLoggedInUserIfNotDisciplined();

        if (loggedInUser == null || !loggedInUser.isHigherAuthority(postingUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DeletePostResponseDto("Error: Insufficient permissions to delete that post.", postToDelete.getId()));
        }

        String postUrl = forumService.getGetDeletedPostUrl(postToDelete);

        postToDelete = forumService.deletePost(postToDelete, loggedInUser);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new DeletePostResponseDto("Post deleted.", postToDelete.getId(), postUrl));
    }

    // TODO: Refactor into PostController
    /**
     * Handles processing of AJAX submission of a restore request on a deleted post.
     */
    @PostMapping(value = "/restorePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestorePostResponseDto> processRestorePost(@RequestBody RestorePostSubmissionDto restorePostSubmissionDto) {
        Post postToRestore = forumService.getPostById(restorePostSubmissionDto.getPostId());

        if (postToRestore == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestorePostResponseDto("Error: Post not found.", null));
        }

        User loggedInUser = getLoggedInUserIfNotDisciplined();
        boolean isValidRestoration = postToRestore.isRestorableBy(loggedInUser);

        if (isValidRestoration) {
            postToRestore = forumService.restorePost(postToRestore);
            String postUrl = forumService.getRestoredPostUrl(postToRestore);

            return ResponseEntity.status(HttpStatus.OK).body(new RestorePostResponseDto("Post restored.", postToRestore.getId(), postUrl));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestorePostResponseDto("Error: Insufficient permissions to restore that post.", postToRestore.getId()));
        }
    }

    // TODO: Refactor into UserController
    /**
     * Handles processing of AJAX submission of a user promotion request.
     */
    @PostMapping(value = "/promoteUserAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processPromoteUser(@RequestBody PromoteUserSubmissionDto promoteUserSubmissionDto) {
        User userToPromote = userService.getUser(promoteUserSubmissionDto.getUsername());
        User loggedInUser = getLoggedInUserIfNotDisciplined();
        UserRole promotableRank = promoteUserSubmissionDto.getPromotableRank();

        if (userToPromote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PromoteUserResponseDto("Error: User not found"));
        }

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PromoteUserResponseDto("Error: Insufficient permissions to promote that user."));
        }

        if (userService.isValidPromotionRequest(loggedInUser, userToPromote, promotableRank)) {
            userToPromote = userService.promoteUser(userToPromote);
            PromoteUserResponseDto purDto = userService.getPromoteUserResponseDtoForUser(userToPromote);

            return ResponseEntity.status(HttpStatus.OK).body(purDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PromoteUserResponseDto("Error: Insufficient permissions to promote that user."));
        }
    }

    // TODO: Refactor into UserController
    /**
     * Handles processing of AJAX submission of a user demotion request.
     */
    @PostMapping(value = "/demoteUserAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processDemoteUser(@RequestBody DemoteUserSubmissionDto demoteUserSubmissionDto) {
        User userToDemote = userService.getUser(demoteUserSubmissionDto.getUsername());
        User loggedInUser = getLoggedInUserIfNotDisciplined();
        UserRole demotableRank = demoteUserSubmissionDto.getDemotableRank();

        if (userToDemote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DemoteUserResponseDto("Error: User not found"));
        }

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DemoteUserResponseDto("Error: Insufficient permissions to demote that user."));
        }

        if (userService.isValidDemotionRequest(loggedInUser, userToDemote, demotableRank)) {
            userToDemote = userService.demoteUser(userToDemote);
            DemoteUserResponseDto durDto = userService.getDemoteUserResponseDtoForUser(userToDemote);

            return ResponseEntity.status(HttpStatus.OK).body(durDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DemoteUserResponseDto("Error: Insufficient permissions to demote that user."));
        }
    }

    // TODO: Refactor into UserController
    /**
     * Provides a promotion button for a user with the given username in an up-to-date state.
     */
    @GetMapping(value = "/demoteUserButton/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView demoteUserButton(@PathVariable String username) {
        User loggedInUser = getLoggedInUserIfNotDisciplined();
        User user = userService.getUser(username);

        ModelAndView mv;

        if (user == null || loggedInUser == null) {
            System.out.println("### Invalid use of /demoteUserButton");
            mv = new ModelAndView();
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }

        UserRankAdjustmentDto userRankAdjustmentDto = userService.getUserRankAdjustmentDtoForUser(user, loggedInUser);

        mv = new ModelAndView("fragments/promote-demote-buttons :: demote-button-fragment");
        mv.setStatus(HttpStatus.OK);
        mv.addObject("userRankAdjustmentDto", userRankAdjustmentDto);
        return mv;
    }

    // TODO: Refactor into UserController
    /**
     * Provides a promotion button for a user with the given username in an up-to-date state.
     */
    @GetMapping(value = "/promoteUserButton/{username}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView promoteUserButton(@PathVariable String username) {
        User loggedInUser = getLoggedInUserIfNotDisciplined();
        User user = userService.getUser(username);

        ModelAndView mv;

        if (user == null || loggedInUser == null) {
            System.out.println("### Invalid use of /promoteUserButton");
            mv = new ModelAndView();
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }

        UserRankAdjustmentDto userRankAdjustmentDto = userService.getUserRankAdjustmentDtoForUser(user, loggedInUser);

        mv = new ModelAndView("fragments/promote-demote-buttons :: promote-button-fragment");
        mv.setStatus(HttpStatus.OK);
        mv.addObject("userRankAdjustmentDto", userRankAdjustmentDto);
        return mv;
    }

    private User getLoggedInUserIfNotDisciplined() {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null || loggedInUser.isBannedOrSuspended()) {
            return null;
        }

        return loggedInUser;
    }
}
