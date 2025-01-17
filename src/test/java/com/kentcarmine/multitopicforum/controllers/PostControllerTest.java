package com.kentcarmine.multitopicforum.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.kentcarmine.multitopicforum.dtos.DeletePostSubmissionDto;
import com.kentcarmine.multitopicforum.dtos.RestorePostSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@ActiveProfiles("test")
class PostControllerTest {
    private static final String ROOT_URL = "localhost:8080";

    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";

    private static final String TEST_TOPIC_THREAD_NAME = "Test Thread Title";

    private static final String TEST_MODERATOR_USERNAME = "TestModerator";
    private static final String TEST_MODERATOR_PASSWORD = "testModPassword";
    private static final String TEST_MODERATOR_EMAIL = "testmoderator@test.com";

    private static final String TEST_MODERATOR_2_USERNAME = "TestModerator2";
    private static final String TEST_MODERATOR_2_PASSWORD = "testMod2Password";
    private static final String TEST_MODERATOR_2_EMAIL = "testmoderator2@test.com";

    private static final String TEST_ADMIN_USERNAME = "TestAdmin";
    private static final String TEST_ADMIN_PASSWORD = "testAdminPassword";
    private static final String TEST_ADMIN_EMAIL = "testadmin@test.com";

    private static final String TEST_SUPER_ADMIN_USERNAME = "TestSuperAdmin";
    private static final String TEST_SUPER_ADMIN_PASSWORD = "testSuperAdminPassword";
    private static final String TEST_SUPER_ADMIN_EMAIL = "testsuperadmin@test.com";

    PostController postController;

    MockMvc mockMvc;

    @Mock
    ForumService forumService;

    @Mock
    UserService userService;

    @Mock
    MessageService messageService;

    @Mock
    TopicThreadService topicThreadService;

    @Mock
    PostService postService;

    @Mock
    PostVoteService postVoteService;

    @Mock
    DisciplineService disciplineService;

    TopicForum testTopicForum;
    TopicThread testTopicForumThread;

    Post testPost;

    User testUser;
    User testModerator;
    User testModerator2;
    User testAdmin;
    User testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        postController = new PostController(forumService, userService, topicThreadService, postService, postVoteService, disciplineService, messageService);

        mockMvc = MockMvcBuilders.standaloneSetup(postController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

        testModerator = new User(TEST_MODERATOR_USERNAME, TEST_MODERATOR_PASSWORD, TEST_MODERATOR_EMAIL);
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testModerator2 = new User(TEST_MODERATOR_2_USERNAME, TEST_MODERATOR_2_PASSWORD, TEST_MODERATOR_2_EMAIL);
        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);

        testSuperAdmin = new User(TEST_SUPER_ADMIN_USERNAME, TEST_SUPER_ADMIN_PASSWORD, TEST_SUPER_ADMIN_EMAIL);
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);

        testUser = new User("testUser", "testPassword", "test@testemail.com");
        testUser.addAuthority(UserRole.USER);

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
        testTopicForumThread = new TopicThread(TEST_TOPIC_THREAD_NAME, testTopicForum);
        testTopicForumThread.setId(2L);
        testTopicForum.addThread(testTopicForumThread);

        testPost = new Post("test post content", java.util.Date.from(Instant.now()));
        testPost.setId(5L);
        testPost.setUser(testUser);
        testPost.setThread(testTopicForumThread);
        testTopicForumThread.getPosts().add(testPost);

        SortedSet<Post> posts = new TreeSet<>();
        Post post2 = new Post("Test Post Title 2", java.util.Date.from(Instant.now().plusSeconds(10)));
        post2.setThread(testTopicForumThread);
        post2.setUser(testUser);
        posts.add(testPost);
        posts.add(post2);
        testTopicForumThread.setId(1l);
        testTopicForumThread.setPosts(posts);
    }

    @Test
    void addPostToThread_validInput() throws Exception {
        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";
        final long newPostId = 20l;

        Post newTestPost = new Post(content, Date.from(Instant.now()));
        newTestPost.setId(newPostId);

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);
        when(postService.addNewPostToThread(any(), any(), any())).thenReturn(newTestPost);

        Page<Post> testPage = new PageImpl<Post>(testTopicForumThread.getPosts().stream().collect(Collectors.toList()));
        when(topicThreadService.getPostPageByThread(any(), anyInt(), anyInt())).thenReturn(testPage);
        int pageNum = testPage.getNumber() + 1;
        System.out.println("### Test Page Num = " + testPage.getNumber());

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForumThread.getForum().getName()
                        + "/show/1?page=" + pageNum + "#post_id_20"));

        verify(postService, times(1)).addNewPostToThread(any(), any(), eq(testTopicForumThread));
    }

    @Test
    void addPostToThread_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));

        verify(postService, times(0)).addNewPostToThread(any(), any(), eq(testTopicForumThread));
    }

    @Test
    void addPostToThread_blankContent() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);
        Page<Post> postPage = new PageImpl<Post>(testTopicForumThread.getPosts().stream().collect(Collectors.toList()));
        when(topicThreadService.getPostPageByThread(any(), anyInt(), anyInt())).thenReturn(postPage);

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

        verify(postService, times(0)).addNewPostToThread(any(), any(), any());
    }

    @Test
    void addPostToThread_noSuchForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);
        when(messageService.getMessage(eq("Exception.forum.notfound"))).thenReturn("Forum was not found.");

        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(postService, times(0)).addNewPostToThread(any(), any(), any());
    }

    @Test
    void addPostToThread_noSuchThreadOnGivenForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(null);
        when(messageService.getMessage(anyString())).thenReturn("Thread was not found.");

        final String content = "Test content";
        final String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/1/createPost";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", content))
                .andExpect(status().isNotFound())
                .andExpect(view().name("thread-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(postService, times(0)).addNewPostToThread(any(), any(), any());
    }

    @Test
    void processDeletePost_invalidPost() throws Exception {
        DeletePostSubmissionDto req = new DeletePostSubmissionDto(117L);

        when(postService.getPostById(anyLong())).thenReturn(null);
        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(messageService.getMessage(anyString())).thenReturn("Post was not found.");

        MvcResult result = mockMvc.perform(post("/deletePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isNotFound())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String postIdStr = JsonPath.read(resStr, "$.postId");
        assertNull(postIdStr);

        String msg = JsonPath.read(resStr, "$.message");
//        assertEquals("Error: Post not found.", msg);
        assertEquals("Post was not found.", msg);

        verify(postService, times(0)).deletePost(any(), any());
    }

    @Test
    void processDeletePost_insufficientAuthority() throws Exception {
        testPost.setUser(testModerator2);
        DeletePostSubmissionDto req = new DeletePostSubmissionDto(testPost.getId());

        when(postService.getPostById(anyLong())).thenReturn(testPost);
        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(messageService.getMessage(any())).thenReturn("You do not have the authority to perform that action.");

        MvcResult result = mockMvc.perform(post("/deletePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();
        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        String msg = JsonPath.read(resStr, "$.message");
//        assertEquals("Error: Insufficient permissions to delete that post.", msg);
        assertEquals("You do not have the authority to perform that action.", msg);

        verify(postService, times(0)).deletePost(any(), any());
    }

    @Test
    void processDeletePost_postAlreadyDeleted() throws Exception {
        testPost.setDeleted(true);
        DeletePostSubmissionDto req = new DeletePostSubmissionDto(testPost.getId());

        String expectedPostUrl = ROOT_URL
                + "/forum/" + testPost.getThread().getForum().getName()
                + "/show/" + testPost.getThread().getId()
                + "#post_id_" + testPost.getId();

        when(postService.getPostById(anyLong())).thenReturn(testPost);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testModerator);
        when(postService.deletePost(any(), any())).thenReturn(testPost);
        when(postService.getGetDeletedPostUrl(any())).thenReturn(expectedPostUrl);
        when(messageService.getMessage("Post.deleted.success")).thenReturn("Post deleted.");

        MvcResult result = mockMvc.perform(post("/deletePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();
        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("Post deleted.", msg);

        String expectedReloadUrlSuffix = "/forum/" + testPost.getThread().getForum().getName()
                + "/show/" + testPost.getThread().getId()
                + "#post_id_" + testPost.getId();

        String reloadUrl = JsonPath.read(resStr, "$.reloadUrl");
        assertTrue(reloadUrl.endsWith(expectedReloadUrlSuffix));

        verify(postService, times(1)).deletePost(any(), any());
    }

    @Test
    void processDeletePost_validDeletion() throws Exception {
        DeletePostSubmissionDto req = new DeletePostSubmissionDto(testPost.getId());

        String expectedPostUrl = ROOT_URL
                + "/forum/" + testPost.getThread().getForum().getName()
                + "/show/" + testPost.getThread().getId()
                + "#post_id_" + testPost.getId();

        when(postService.getPostById(anyLong())).thenReturn(testPost);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testModerator);
        when(postService.deletePost(any(), any())).thenReturn(testPost);
        when(postService.getGetDeletedPostUrl(any())).thenReturn(expectedPostUrl);
        when(messageService.getMessage(eq("Post.deleted.success"))).thenReturn("Post deleted.");

        MvcResult result = mockMvc.perform(post("/deletePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();
        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("Post deleted.", msg);

        String expectedReloadUrlSuffix = "/forum/" + testPost.getThread().getForum().getName()
                + "/show/" + testPost.getThread().getId()
                + "#post_id_" + testPost.getId();

        String reloadUrl = JsonPath.read(resStr, "$.reloadUrl");
        assertTrue(reloadUrl.endsWith(expectedReloadUrlSuffix));

        verify(postService, times(1)).deletePost(any(), any());
    }

    @Test
    void processRestorePost_invalidPost() throws Exception {
        RestorePostSubmissionDto req = new RestorePostSubmissionDto(192L);

        when(postService.getPostById(anyLong())).thenReturn(null);
        when(userService.getLoggedInUser()).thenReturn(testModerator);

        MvcResult result = mockMvc.perform(post("/restorePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isNotFound())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String postIdStr = JsonPath.read(resStr, "$.postId");
        assertNull(postIdStr);

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("Error: Post not found.", msg);

        verify(postService, times(0)).restorePost(any());
    }

    @Test
    void processRestorePost_insufficientAuthority() throws Exception {
        User deletingUser = testModerator2;
        java.util.Date deletedAt = java.util.Date.from(Instant.now());

        testPost.setDeleted(true);
        testPost.setDeletedBy(deletingUser);
        testPost.setDeletedAt(deletedAt);

        RestorePostSubmissionDto req = new RestorePostSubmissionDto(192L);

        when(postService.getPostById(anyLong())).thenReturn(testPost);
        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(messageService.getMessage(eq("Exception.authority.insufficient"))).thenReturn("You do not have the authority to perform that action.");

        MvcResult result = mockMvc.perform(post("/restorePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("You do not have the authority to perform that action.", msg);

        verify(postService, times(0)).restorePost(any());
    }

    @Test
    void processRestorePost_validRestoration() throws Exception {
        User deletingUser = testModerator2;
        java.util.Date deletedAt = java.util.Date.from(Instant.now());

        testPost.setDeleted(true);
        testPost.setDeletedBy(deletingUser);
        testPost.setDeletedAt(deletedAt);

        Post testPostRestored = new Post(testPost.getContent(), testPost.getPostedAt());
        testPostRestored.setId(testPost.getId());
        testPostRestored.setThread(testPost.getThread());
        testPostRestored.setUser(testPost.getUser());
        testPostRestored.setPostVotes(testPost.getPostVotes());
        testPostRestored.setDeleted(false);
        testPostRestored.setDeletedBy(null);
        testPostRestored.setDeletedAt(null);

        String expectedRestoredPostUrl = ROOT_URL
                + "/forum/" + testPostRestored.getThread().getForum().getName()
                + "/show/" + testPostRestored.getThread().getId()
                + "#post_id_" + testPostRestored.getId();

        RestorePostSubmissionDto req = new RestorePostSubmissionDto(192L);

        when(postService.getPostById(anyLong())).thenReturn(testPost);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);
        when(postService.restorePost(any())).thenReturn(testPostRestored);
        when(postService.getRestoredPostUrl(any())).thenReturn(expectedRestoredPostUrl);
        when(messageService.getMessage(eq("Post.restored.success"))).thenReturn("Post restored.");

        MvcResult result = mockMvc.perform(post("/restorePostAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        Long postId = Long.parseLong(JsonPath.read(resStr, "$.postId").toString());
        assertEquals(testPost.getId(), postId);

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("Post restored.", msg);

        String expectedReloadUrlSuffix = "/forum/" + testPost.getThread().getForum().getName()
                + "/show/" + testPost.getThread().getId()
                + "#post_id_" + testPost.getId();

        String reloadUrl = JsonPath.read(resStr, "$.reloadUrl");
        assertTrue(reloadUrl.endsWith(expectedReloadUrlSuffix));

        verify(postService, times(1)).restorePost(any());
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