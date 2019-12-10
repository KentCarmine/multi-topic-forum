package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.services.ForumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class ForumControllerTest {
    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";

    ForumController forumController;

    MockMvc mockMvc;

    @Mock
    ForumService forumService;

    @Mock
    MessageSource messageSource;

    TopicForum testTopicForum;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumController = new ForumController(forumService);

        mockMvc = MockMvcBuilders.standaloneSetup(forumController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageSource)).build();

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
    }

    @Test
    void showCreateNewForumPage() throws Exception {
        mockMvc.perform(get("/createNewForum"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-new-forum-page"))
                .andExpect(model().attributeExists("topicForumDto"));
    }

    @Test
    void processNewForumCreation_validInput() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", TEST_TOPIC_FORUM_NAME)
                .param("description", TEST_TOPIC_FORUM_DESC))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + TEST_TOPIC_FORUM_NAME));
    }

    @Test
    void processNewForumCreation_shortName() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "a")
                .param("description", TEST_TOPIC_FORUM_DESC))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-new-forum-page"));
    }

    @Test
    void processNewForumCreation_blankName() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "     ")
                .param("description", TEST_TOPIC_FORUM_DESC))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-new-forum-page"));
    }

    @Test
    void processNewForumCreation_invalidCharactersInName() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "^-an_] invalid{%name")
                .param("description", TEST_TOPIC_FORUM_DESC))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-new-forum-page"));
    }

    @Test
    void processNewForumCreation_duplicateName() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", TEST_TOPIC_FORUM_NAME)
                .param("description", TEST_TOPIC_FORUM_DESC))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-new-forum-page"));
    }

    @Test
    void processNewForumCreation_shortDescription() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", TEST_TOPIC_FORUM_NAME)
                .param("description", ""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-new-forum-page"));
    }

    @Test
    void processNewForumCreation_blankDescription() throws Exception {
        when(forumService.createForumByDto(any())).thenReturn(testTopicForum);

        mockMvc.perform(post("/processNewForumCreation")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", TEST_TOPIC_FORUM_NAME)
                .param("description", "   "))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-new-forum-page"));
    }

    @Test
    void showForum_existingForum() throws Exception {
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);

        mockMvc.perform(get("/forum/" + testTopicForum.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("forum-page"))
                .andExpect(model().attributeExists("forum"));
    }

    @Test
    void showForum_nonExistingForum() throws Exception {
        when(forumService.getForumByName(anyString())).thenReturn(null);

        mockMvc.perform(get("/forum/" + testTopicForum.getName()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));

    }
}