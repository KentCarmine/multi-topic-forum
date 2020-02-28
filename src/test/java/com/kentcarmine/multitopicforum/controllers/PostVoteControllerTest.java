package com.kentcarmine.multitopicforum.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class PostVoteControllerTest {

    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

//    private static final String TEST_MODERATOR_USERNAME = "TestModerator";
//    private static final String TEST_MODERATOR_PASSWORD = "testModPassword";
//    private static final String TEST_MODERATOR_EMAIL = "testmoderator@test.com";
//
//    private static final String TEST_MODERATOR_2_USERNAME = "TestModerator2";
//    private static final String TEST_MODERATOR_2_PASSWORD = "testMod2Password";
//    private static final String TEST_MODERATOR_2_EMAIL = "testmoderator2@test.com";
//
//    private static final String TEST_ADMIN_USERNAME = "TestAdmin";
//    private static final String TEST_ADMIN_PASSWORD = "testAdminPassword";
//    private static final String TEST_ADMIN_EMAIL = "testadmin@test.com";

    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";
//    private static final String TEST_TOPIC_FORUM_NAME_2 = "TestName2";
//    private static final String TEST_TOPIC_FORUM_DESC_2 = "Description of test topic forum 2";
    private static final String TEST_TOPIC_THREAD_NAME = "Test Thread Name";
//    private static final String TEST_TOPIC_THREAD_NAME_2 = "Test Thread Name 2";

    @Mock
    ForumService forumService;

    @Mock
    UserService userService;

    //    @Mock
//    MessageSource messageSource;
    @Mock
    MessageService messageService;

    @Mock
    PostService postService;

    @Mock
    PostVoteService postVoteService;

    PostVoteController postVoteController;

    MockMvc mockMvc;

    TopicForum testTopicForum;
    TopicThread testTopicThread;

    Post testPost;

    User testUser;
//    User testModerator;
//    User testModerator2;
//    User testAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        postVoteController = new PostVoteController(forumService, userService, postService, postVoteService);
        mockMvc = MockMvcBuilders.standaloneSetup(postVoteController)
                .setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
        testTopicThread = new TopicThread(TEST_TOPIC_THREAD_NAME, testTopicForum);
        testTopicThread.setId(2L);
        testTopicForum.addThread(testTopicThread);

        testPost = new Post("test post content", Date.from(Instant.now()));
        testPost.setId(5L);
        testPost.setUser(testUser);
        testPost.setThread(testTopicThread);
        testTopicThread.getPosts().add(testPost);

//        testModerator = new User(TEST_MODERATOR_USERNAME, TEST_MODERATOR_PASSWORD, TEST_MODERATOR_EMAIL);
//        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);
//
//        testModerator2 = new User(TEST_MODERATOR_2_USERNAME, TEST_MODERATOR_2_PASSWORD, TEST_MODERATOR_2_EMAIL);
//        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);
//
//        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
//        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);
    }

    @Test
    void processVoteSubmission_validVote() throws Exception {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.UPVOTE.getValue());

//        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testUser);
        when(postService.getPostById(anyLong())).thenReturn(testPost);
        PostVoteResponseDto responseDto = new PostVoteResponseDto(req.getPostId(), true, false, true, 1);
        when(postVoteService.handlePostVoteSubmission(any(), any(), any())).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(post("/handleVoteAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        boolean hasUpvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasUpvote").toString());
        assertTrue(hasUpvote);

        boolean hasDownvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasDownvote").toString());
        assertFalse(hasDownvote);

        boolean isVoteUpdated = Boolean.parseBoolean(JsonPath.read(resStr, "$.voteUpdated").toString());
        assertTrue(isVoteUpdated);

        int voteTotal = Integer.parseInt(JsonPath.read(resStr, "$.voteTotal").toString());
        assertEquals(1, voteTotal);

        verify(userService, times(1)).getLoggedInUserIfNotDisciplined();
        verify(postService, times(1)).getPostById(anyLong());
        verify(postVoteService, times(1)).getPostVoteByUserAndPost(any(), any());
        verify(postVoteService, times(1)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_repeatedVote() throws Exception  {
        PostVote existingVote = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
        existingVote.setId(3L);
        testPost.addPostVote(existingVote);
        testUser.getPostVotes().add(existingVote);

        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.DOWNVOTE.getValue());

//        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testUser);
        when(postService.getPostById(anyLong())).thenReturn(testPost);
        when(postVoteService.getPostVoteByUserAndPost(testUser, testPost)).thenReturn(existingVote);

        MvcResult result = mockMvc.perform(post("/handleVoteAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        boolean hasUpvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasUpvote").toString());
        assertEquals(existingVote.isUpvote(), hasUpvote);

        boolean hasDownvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasDownvote").toString());
        assertEquals(existingVote.isDownvote(), hasDownvote);

        boolean isVoteUpdated = Boolean.parseBoolean(JsonPath.read(resStr, "$.voteUpdated").toString());
        assertFalse(isVoteUpdated);

        int voteTotal = Integer.parseInt(JsonPath.read(resStr, "$.voteTotal").toString());
        assertEquals(1, voteTotal);

        verify(userService, times(1)).getLoggedInUserIfNotDisciplined();
        verify(postService, times(1)).getPostById(anyLong());
        verify(postVoteService, times(1)).getPostVoteByUserAndPost(any(), any());
        verify(postVoteService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_invalidVoteValue() throws Exception  {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), 0);

//        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testUser);
        when(postService.getPostById(anyLong())).thenReturn(testPost);

        MvcResult result = mockMvc.perform(post("/handleVoteAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String postIdStr = JsonPath.read(resStr, "$.postId");
        assertNull(postIdStr);

        boolean hasUpvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasUpvote").toString());
        assertFalse(hasUpvote);

        boolean hasDownvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasDownvote").toString());
        assertFalse(hasDownvote);

        boolean isVoteUpdated = Boolean.parseBoolean(JsonPath.read(resStr, "$.voteUpdated").toString());
        assertFalse(isVoteUpdated);

        int voteTotal = Integer.parseInt(JsonPath.read(resStr, "$.voteTotal").toString());
        assertEquals(0, voteTotal);

        verify(userService, times(1)).getLoggedInUserIfNotDisciplined();
        verify(postService, times(1)).getPostById(anyLong());
        verify(postVoteService, times(0)).getPostVoteByUserAndPost(any(), any());
        verify(postVoteService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_noUser() throws Exception  {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.DOWNVOTE.getValue());

//        when(userService.getLoggedInUser()).thenReturn(null);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(null);
        when(postService.getPostById(anyLong())).thenReturn(testPost);

        MvcResult result = mockMvc.perform(post("/handleVoteAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String postIdStr = JsonPath.read(resStr, "$.postId");
        assertNull(postIdStr);

        boolean hasUpvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasUpvote").toString());
        assertFalse(hasUpvote);

        boolean hasDownvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasDownvote").toString());
        assertFalse(hasDownvote);

        boolean isVoteUpdated = Boolean.parseBoolean(JsonPath.read(resStr, "$.voteUpdated").toString());
        assertFalse(isVoteUpdated);

        int voteTotal = Integer.parseInt(JsonPath.read(resStr, "$.voteTotal").toString());
        assertEquals(0, voteTotal);

        verify(userService, times(1)).getLoggedInUserIfNotDisciplined();
        verify(postService, times(1)).getPostById(anyLong());
        verify(postVoteService, times(0)).getPostVoteByUserAndPost(any(), any());
        verify(postVoteService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_noPost() throws Exception  {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.UPVOTE.getValue());

//        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testUser);
        when(postService.getPostById(anyLong())).thenReturn(null);

        MvcResult result = mockMvc.perform(post("/handleVoteAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String postIdStr = JsonPath.read(resStr, "$.postId");
        assertNull(postIdStr);

        boolean hasUpvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasUpvote").toString());
        assertFalse(hasUpvote);

        boolean hasDownvote = Boolean.parseBoolean(JsonPath.read(resStr, "$.hasDownvote").toString());
        assertFalse(hasDownvote);

        boolean isVoteUpdated = Boolean.parseBoolean(JsonPath.read(resStr, "$.voteUpdated").toString());
        assertFalse(isVoteUpdated);

        int voteTotal = Integer.parseInt(JsonPath.read(resStr, "$.voteTotal").toString());
        assertEquals(0, voteTotal);

        verify(userService, times(1)).getLoggedInUserIfNotDisciplined();
        verify(postService, times(1)).getPostById(anyLong());
        verify(postVoteService, times(0)).getPostVoteByUserAndPost(any(), any());
        verify(postVoteService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    /**
     * Helper method to convert objects into JSON strings.
     *
     * @param obj the object to convert
     * @return the JSON string representing obj
     */
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}