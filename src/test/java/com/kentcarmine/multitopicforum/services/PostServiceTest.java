package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.PostVoteRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class PostServiceTest {

    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";
    private static final String TEST_TOPIC_THREAD_NAME = "Test Thread Name";

    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

    private static final String TEST_MODERATOR_USERNAME = "TestModerator";
    private static final String TEST_MODERATOR_PASSWORD = "testModPassword";
    private static final String TEST_MODERATOR_EMAIL = "testmoderator@test.com";

    private static final String TEST_MODERATOR_2_USERNAME = "TestModerator2";
    private static final String TEST_MODERATOR_2_PASSWORD = "testMod2Password";
    private static final String TEST_MODERATOR_2_EMAIL = "testmoderator2@test.com";

    private static final String TEST_ADMIN_USERNAME = "TestAdmin";
    private static final String TEST_ADMIN_PASSWORD = "testAdminPassword";
    private static final String TEST_ADMIN_EMAIL = "testadmin@test.com";

    @Mock
    PostRepository postRepository;

    PostService postService;

    private TopicForum testTopicForum;
    private TopicThread testTopicThread;
    private Post testPost;

    private User testUser;
    private User testModerator;
    private User testModerator2;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        postService = new PostServiceImpl(postRepository);

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);

        testModerator = new User(TEST_MODERATOR_USERNAME, TEST_MODERATOR_PASSWORD, TEST_MODERATOR_EMAIL);
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testModerator2 = new User(TEST_MODERATOR_2_USERNAME, TEST_MODERATOR_2_PASSWORD, TEST_MODERATOR_2_EMAIL);
        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
        testTopicThread = new TopicThread(TEST_TOPIC_THREAD_NAME, testTopicForum);
        testPost = new Post("test post content", Date.from(Instant.now()));
        testPost.setId(1L);
        testPost.setUser(testUser);
        testTopicThread.getPosts().add(testPost);
        testTopicForum.addThread(testTopicThread);
    }

    @Test
    void addNewPostToThread() throws Exception {
        final String testContent = "test content";
        PostCreationDto postCreationDto = new PostCreationDto(testContent);
        final User testUser = new User("testUser", "password", "test@test.com");
        testUser.addAuthority(UserRole.USER);

        postService.addNewPostToThread(postCreationDto, testUser, testTopicThread);

        verify(postRepository, times(1)).save(any());
    }

    @Test
    void deletePost() throws Exception {
        assertFalse(testPost.isDeleted());

        Date deletedAtTimestamp = Date.from(Instant.now());
        User deletingUser = testModerator;

        Post testPostDeleted = new Post(testPost.getContent(), testPost.getPostedAt());
        testPostDeleted.setId(testPost.getId());
        testPostDeleted.setThread(testPost.getThread());
        testPostDeleted.setUser(testPost.getUser());
        testPostDeleted.setPostVotes(testPost.getPostVotes());
        testPostDeleted.setDeleted(true);
        testPostDeleted.setDeletedBy(deletingUser);
        testPostDeleted.setDeletedAt(deletedAtTimestamp);

        when(postRepository.save(any())).thenReturn(testPostDeleted);

        Post result = postService.deletePost(testPost, deletingUser);

        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getContent(), result.getContent());
        assertEquals(testPost.getPostedAt(), result.getPostedAt());
        assertEquals(testPost.getThread(), result.getThread());
        assertEquals(testPost.getUser(), result.getUser());
        assertTrue(result.isDeleted());
        assertEquals(deletingUser, result.getDeletedBy());
        assertEquals(deletedAtTimestamp, result.getDeletedAt());

        verify(postRepository, times(1)).save(any());
    }

    @Test
    void deletePost_alreadyDeleted() throws Exception {
        testPost.setDeleted(true);

        postService.deletePost(testPost, testModerator);

        verify(postRepository, times(0)).save(any());
    }

    @Test
    void restorePost() throws Exception {
        Date deletedAtTimestamp =  Date.from(Instant.now());
        User deletingUser = testModerator;
        testPost.setDeleted(true);
        testPost.setDeletedAt(deletedAtTimestamp);
        testPost.setDeletedBy(deletingUser);

        Post testPostRestored = new Post(testPost.getContent(), testPost.getPostedAt());
        testPostRestored.setId(testPost.getId());
        testPostRestored.setThread(testPost.getThread());
        testPostRestored.setUser(testPost.getUser());
        testPostRestored.setPostVotes(testPost.getPostVotes());
        testPostRestored.setDeleted(false);
        testPostRestored.setDeletedBy(null);
        testPostRestored.setDeletedAt(null);

        when(postRepository.save(any())).thenReturn(testPostRestored);

        Post result = postService.restorePost(testPost);

        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getContent(), result.getContent());
        assertEquals(testPost.getPostedAt(), result.getPostedAt());
        assertEquals(testPost.getThread(), result.getThread());
        assertEquals(testPost.getUser(), result.getUser());
        assertFalse(result.isDeleted());
        assertNull(result.getDeletedBy());
        assertNull(result.getDeletedAt());

        verify(postRepository, times(1)).save(any());
    }

}