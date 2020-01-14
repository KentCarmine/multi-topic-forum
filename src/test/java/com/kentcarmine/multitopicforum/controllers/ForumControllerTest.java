package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
    UserService userService;

    @Mock
    MessageSource messageSource;

    TopicForum testTopicForum;
    User testUser;
    TopicThread testTopicForumThread;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumController = new ForumController(forumService, userService);

        mockMvc = MockMvcBuilders.standaloneSetup(forumController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageSource)).build();

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
        testUser = new User("testUser", "testPassword", "test@testemail.com");
        testUser.addAuthority(UserRole.USER);
        testTopicForumThread = new TopicThread("Test Thread Title", testTopicForum);
        SortedSet<Post> posts = new TreeSet<>();
        Post post = new Post("Test Post Title", Date.from(Instant.now()));
        post.setThread(testTopicForumThread);
        post.setUser(testUser);
        Post post2 = new Post("Test Post Title 2", java.util.Date.from(Instant.now().plusSeconds(10)));
        post2.setThread(testTopicForumThread);
        post2.setUser(testUser);
        posts.add(post);
        posts.add(post2);
        testTopicForumThread.setId(1l);
        testTopicForumThread.setPosts(posts);
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

    @Test
    void showCreateThreadPage() throws Exception {
        mockMvc.perform(get("/forum/" + testTopicForum.getName() + "/createThread"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-thread-page"))
                .andExpect(model().attributeExists("topicThreadCreationDto"))
                .andExpect(model().attributeExists("forumName"));
    }

    @Test
    void processCreateThread_validInput() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(forumService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName()
                        + "/show/" + testTopicForumThread.getId()));
    }

    @Test
    void processCreateThread_noSuchForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(forumService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void processCreateThread_blankThreadTitle() throws Exception {
        testTopicForumThread.setTitle("     ");

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(forumService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-thread-page"))
                .andExpect(model().hasErrors());
    }

    @Test
    void processCreateThread_shortThreadTitle() throws Exception {
        testTopicForumThread.setTitle("1");

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(forumService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-thread-page"))
                .andExpect(model().hasErrors());
    }

    @Test
    void processCreateThread_blankContent() throws Exception {
        testTopicForumThread.getPosts().first().setContent("     ");

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(forumService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-thread-page"))
                .andExpect(model().hasErrors());
    }

    @Test
    void showThread_validThread() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("topic-thread-page"))
                .andExpect(model().attributeExists("forumName"))
                .andExpect(model().attributeExists("threadId"))
                .andExpect(model().attributeExists("threadTitle"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void showThread_noSuchForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void showThread_noSuchThreadOnGivenForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(null);

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andExpect(view().name("thread-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void addPostToThread_validInput() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForumThread.getForum().getName() + "/show/1"));

        verify(forumService, times(1)).addNewPostToThread(any(), any(), eq(testTopicForumThread));
    }

    @Test
    void addPostToThread_blankContent() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", ""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("topic-thread-page"))
                .andExpect(model().attributeExists("postCreationDto"))
                .andExpect(model().attributeExists("forumName"))
                .andExpect(model().attributeExists("threadTitle"))
                .andExpect(model().attributeExists("threadId"))
                .andExpect(model().attributeExists("posts"));

        verify(forumService, times(0)).addNewPostToThread(any(), any(), any());
    }

    @Test
    void addPostToThread_noSuchForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(forumService, times(0)).addNewPostToThread(any(), any(), any());
    }

    @Test
    void addPostToThread_noSuchThreadOnGivenForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(forumService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(null);

        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().isNotFound())
                .andExpect(view().name("thread-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(forumService, times(0)).addNewPostToThread(any(), any(), any());
    }

    @Test
    void processTopicForumSearch_validSearch() throws Exception {
        final String searchText = "  \"Description of test \"  ";
        mockMvc.perform(post("/searchTopicForums")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", searchText))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forums?search=" + URLEncoderDecoderHelper.encode(searchText.trim())));
    }

    @Test
    void processTopicForumSearch_invalidSearch() throws Exception {
        final String searchText = "\"";
        mockMvc.perform(post("/searchTopicForums")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", searchText))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forums?searchError"));
    }

    @Test
    void showForumsPage_allForums() throws Exception {
        mockMvc.perform(get("/forums"))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(1)).getAllForums();
        verify(forumService, times(0)).searchTopicForums(anyString());
    }

    @Test
    void showForumsPage_validForumSearch() throws Exception {
        String searchString = URLEncoderDecoderHelper.encode(" \"Description of test \"   ");
        SortedSet<TopicForum> forumsResults = new TreeSet<>(new Comparator<TopicForum>() {
            @Override
            public int compare(TopicForum o1, TopicForum o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        forumsResults.add(testTopicForum);

        when(forumService.searchTopicForums(anyString())).thenReturn(forumsResults);

        mockMvc.perform(get("/forums?search=" + searchString))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(0)).getAllForums();
        verify(forumService, times(1)).searchTopicForums(anyString());
    }

    @Test
    void showForumsPage_invalidForumSearch() throws Exception {
        mockMvc.perform(get("/forums?searchError"))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(1)).getAllForums();
        verify(forumService, times(0)).searchTopicForums(anyString());
    }

    @Test
    void showForumsPage_emptyStringForumSearch() throws Exception {
        mockMvc.perform(get("/forums?search="))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(1)).getAllForums();
        verify(forumService, times(0)).searchTopicForums(anyString());
    }
}