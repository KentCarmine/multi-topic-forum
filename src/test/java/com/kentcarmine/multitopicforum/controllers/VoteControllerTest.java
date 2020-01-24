package com.kentcarmine.multitopicforum.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class VoteControllerTest {
    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";


    @Mock
    ForumService forumService;

    @Mock
    UserService userService;

    @Mock
    MessageSource messageSource;

    VoteController voteController;

    MockMvc mockMvc;

    Post testPost;

    User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        voteController = new VoteController(forumService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(voteController)
                .setControllerAdvice(new CustomResponseEntityExceptionHandler(messageSource)).build();

        testPost = new Post("test post content", Date.from(Instant.now()));
        testPost.setId(5L);

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);
    }

    @Test
    void processVoteSubmission_validVote() throws Exception {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.UPVOTE.getValue());

        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getPostById(anyLong())).thenReturn(testPost);
        PostVoteResponseDto responseDto = new PostVoteResponseDto(req.getPostId(), true, false, true, 1);
        when(forumService.handlePostVoteSubmission(any(), any(), any())).thenReturn(responseDto);

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

        verify(userService, times(1)).getLoggedInUser();
        verify(forumService, times(1)).getPostById(anyLong());
        verify(forumService, times(1)).getPostVoteByUserAndPost(any(), any());
        verify(forumService, times(1)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_repeatedVote() throws Exception  {
        PostVote existingVote = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
        existingVote.setId(3L);
        testPost.addPostVote(existingVote);
        testUser.getPostVotes().add(existingVote);

        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.DOWNVOTE.getValue());

        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getPostById(anyLong())).thenReturn(testPost);
        when(forumService.getPostVoteByUserAndPost(testUser, testPost)).thenReturn(existingVote);

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

        verify(userService, times(1)).getLoggedInUser();
        verify(forumService, times(1)).getPostById(anyLong());
        verify(forumService, times(1)).getPostVoteByUserAndPost(any(), any());
        verify(forumService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_invalidVoteValue() throws Exception  {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), 0);

        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getPostById(anyLong())).thenReturn(testPost);

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

        verify(userService, times(1)).getLoggedInUser();
        verify(forumService, times(1)).getPostById(anyLong());
        verify(forumService, times(0)).getPostVoteByUserAndPost(any(), any());
        verify(forumService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_noUser() throws Exception  {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.DOWNVOTE.getValue());

        when(userService.getLoggedInUser()).thenReturn(null);
        when(forumService.getPostById(anyLong())).thenReturn(testPost);

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

        verify(userService, times(1)).getLoggedInUser();
        verify(forumService, times(1)).getPostById(anyLong());
        verify(forumService, times(0)).getPostVoteByUserAndPost(any(), any());
        verify(forumService, times(0)).handlePostVoteSubmission(any(), any(), any());
    }

    @Test
    void processVoteSubmission_noPost() throws Exception  {
        PostVoteSubmissionDto req = new PostVoteSubmissionDto(testPost.getId(), PostVoteState.UPVOTE.getValue());

        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getPostById(anyLong())).thenReturn(null);

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

        verify(userService, times(1)).getLoggedInUser();
        verify(forumService, times(1)).getPostById(anyLong());
        verify(forumService, times(0)).getPostVoteByUserAndPost(any(), any());
        verify(forumService, times(0)).handlePostVoteSubmission(any(), any(), any());
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