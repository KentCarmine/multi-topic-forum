package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.ForumNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.TopicThreadNotFoundException;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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

/**
 * Controller that handles requests related to Posts.
 */
@Controller
public class PostController {

    @Value("${spring.data.web.pageable.default-page-size}")
    private int POSTS_PER_PAGE;

    private final ForumService forumService;
    private final UserService userService;
    private final TopicThreadService topicThreadService;
    private final PostService postService;
    private final PostVoteService postVoteService;
    private final DisciplineService disciplineService;
    private final MessageService messageService;

    @Autowired
    public PostController(ForumService forumService, UserService userService, TopicThreadService topicThreadService,
                          PostService postService, PostVoteService postVoteService, DisciplineService disciplineService, MessageService messageService) {
        this.forumService = forumService;
        this.userService = userService;
        this.topicThreadService = topicThreadService;
        this.postService = postService;
        this.postVoteService = postVoteService;
        this.disciplineService = disciplineService;
        this.messageService = messageService;
    }

    /**
     * Handle processing of form submission for adding a new post to the current thread
     */
    @PostMapping("/forum/{forumName}/show/{threadId}/createPost")
    public ModelAndView addPostToThread(@Valid @ModelAttribute PostCreationDto postCreationDto, BindingResult bindingResult, @PathVariable String forumName,
                                        @PathVariable Long threadId) {
        System.out.println("### in addPostToThread()");
        ModelAndView mv;

        if (!forumService.isForumWithNameExists(forumName)) {
//            throw new ForumNotFoundException("Forum " + forumName + " does not exist");
            throw new ForumNotFoundException();
        }

        TopicThread thread = topicThreadService.getThreadByForumNameAndId(forumName, threadId);

        if (thread == null) {
//            throw new TopicThreadNotFoundException("Thread was not found");
            throw new TopicThreadNotFoundException();
        }

        int userLastViewingPageNum = 1; // TODO: Update this (last page number the user was looking at, for error path)

        if (bindingResult.hasErrors()) {
            System.out.println("### in addPostToThread(). hasErrors() case");
            bindingResult.getAllErrors().stream().forEach(System.out::println);

            Page<Post> posts = topicThreadService.getPostPage(thread, userLastViewingPageNum, POSTS_PER_PAGE);

            mv = new ModelAndView("topic-thread-page", "postCreationDto", postCreationDto);
            mv.addObject("forumName", forumName);
            mv.addObject("threadTitle", thread.getTitle());
            mv.addObject("threadId", threadId);
            mv.addObject("threadIsLocked", thread.isLocked());
//            mv.addObject("posts", thread.getPosts());
            mv.addObject("posts", posts);

            // TODO: Add url param to mv of userLastViewingPageNum; [NOTE: Probably not needed, as is being passed in via DTO]

            User loggedInUser = userService.getLoggedInUser();

            if (loggedInUser != null) {
                System.out.println("### in addPostToThread(). has loggedInUser case");
                mv.addObject("loggedInUser", loggedInUser);
                mv.addObject("voteMap", postVoteService.generateVoteMap(loggedInUser, thread));
                mv.addObject("canLock", topicThreadService.canUserLockThread(loggedInUser, thread));
                mv.addObject("canUnlock", topicThreadService.canUserUnlockThread(loggedInUser, thread));
            }

            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            return mv;
        }
        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        Post newPost = postService.addNewPostToThread(postCreationDto, loggedInUser, thread);
        String newPostId = "#post_id_" + newPost.getId();

        Page<Post> posts = topicThreadService.getPostPage(thread, userLastViewingPageNum, POSTS_PER_PAGE);
        int finalPageNum = posts.getTotalPages(); // TODO: Update this (maximum page number, for happy path) [NOTE: Check that newly added post is counted correctly if it results in the creation of a new page]

        mv = new ModelAndView("redirect:/forum/" + forumName + "/show/" + threadId + newPostId); // TODO: Update with finalPageNum url param
        return mv;
    }

    /**
     * Handles processing of AJAX submission of a delete request on a post.
     */
    @PostMapping(value = "/deletePostAjax", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletePostResponseDto> processDeletePost(@RequestBody DeletePostSubmissionDto deletePostSubmissionDto) {
        Post postToDelete = postService.getPostById(deletePostSubmissionDto.getPostId());

        if (postToDelete == null) {
            String msg = messageService.getMessage("Exception.post.notfound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DeletePostResponseDto(msg, null));
        }

        User postingUser = postToDelete.getUser();
        User loggedInUser = userService.getLoggedInUserIfNotDisciplined();

        if (loggedInUser == null || !loggedInUser.isHigherAuthority(postingUser)) {
            String msg = messageService.getMessage("Exception.authority.insufficient");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DeletePostResponseDto(msg, postToDelete.getId()));
        }

        String postUrl = postService.getGetDeletedPostUrl(postToDelete);

        postToDelete = postService.deletePost(postToDelete, loggedInUser);

        String msg = messageService.getMessage("Post.deleted.success");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new DeletePostResponseDto(msg, postToDelete.getId(), postUrl));
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

            String msg = messageService.getMessage("Post.restored.success");
            return ResponseEntity.status(HttpStatus.OK).body(new RestorePostResponseDto(msg, postToRestore.getId(), postUrl));
        } else {
            String msg = messageService.getMessage("Exception.authority.insufficient");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestorePostResponseDto(msg, postToRestore.getId()));
        }
    }
}
