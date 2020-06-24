package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDtoLight;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class TopicThreadServiceTest {

    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";
    private static final String TEST_TOPIC_FORUM_NAME_2 = "TestName2";
    private static final String TEST_TOPIC_FORUM_DESC_2 = "Description of test topic forum 2";
    private static final String TEST_TOPIC_THREAD_NAME = "Test Thread Name";
    private static final String TEST_TOPIC_THREAD_NAME_2 = "Test Thread Name 2";

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

    TopicThreadService topicThreadService;

    @Mock
    TopicForumRepository topicForumRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    TopicThreadRepository topicThreadRepository;

    @Mock
    ForumService forumService;

    ForumHierarchyConverter forumHierarchyConverter;

    @Mock
    TimeCalculatorService timeCalculatorService;

    private TopicForum testTopicForum;
    private TopicForum testTopicForum2;
    private TopicThread testTopicThread;
    private TopicThread testTopicThread2;
    private Post testPost;

    private User testUser;
    private User testModerator;
    private User testModerator2;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumHierarchyConverter = new ForumHierarchyConverter();

        topicThreadService = new TopicThreadServiceImpl(topicForumRepository, topicThreadRepository, postRepository, forumService, forumHierarchyConverter, timeCalculatorService);

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
        testPost.setThread(testTopicThread);
        testTopicThread.getPosts().add(testPost);
        testTopicForum.addThread(testTopicThread);

        testTopicForum2 = new TopicForum(TEST_TOPIC_FORUM_NAME_2, TEST_TOPIC_FORUM_DESC_2);
        testTopicThread2 = new TopicThread(TEST_TOPIC_THREAD_NAME_2, testTopicForum2);
    }

    @Test
    void createNewTopicThread() throws Exception {
        final String postContent = "test post content";
        User user = new User("TestUser", "testPassword", "test@test.com");
        user.addAuthority(UserRole.USER);

        TopicThreadCreationDto thread = new TopicThreadCreationDto(TEST_TOPIC_THREAD_NAME, postContent);
        topicThreadService.createNewTopicThread(thread, user, testTopicForum);

        verify(topicThreadRepository, times(1)).save(any());
        verify(postRepository, times(1)).save(any());
    }

    @Test
    void getThreadByForumNameAndId_valid() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread));
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        TopicThread result = topicThreadService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNotNull(result);
        assertEquals(testTopicThread, result);
    }

    @Test
    void getThreadByForumNameAndId_threadDoesNotBelongToForum() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread2));
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        TopicThread result = topicThreadService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNull(result);
    }

    @Test
    void getThreadByForumNameAndId_threadDoesNotExist() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        TopicThread result = topicThreadService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNull(result);
    }

    @Test
    void getThreadByForumNameAndId_forumDoesNotExist() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread));
        when(topicForumRepository.findByName(anyString())).thenReturn(null);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);

        TopicThread result = topicThreadService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNull(result);
    }

    @Test
    void searchTopicThreads_existingResults() throws Exception {
        when(topicForumRepository.findByName(eq(TEST_TOPIC_FORUM_NAME))).thenReturn(testTopicForum);
        when(topicThreadRepository.findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(testTopicThread));

        final String searchStr = "test";

        SortedSet<TopicThreadViewDto> results = topicThreadService.searchTopicThreads(TEST_TOPIC_FORUM_NAME, searchStr);

        assertEquals(1, results.size());
        assertEquals(testTopicThread.getTitle(), results.first().getTitle());

        verify(topicThreadRepository, times(1))
                .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchTopicThreads_noResults() throws Exception {
        when(topicForumRepository.findByName(eq(TEST_TOPIC_FORUM_NAME))).thenReturn(testTopicForum);
        when(topicThreadRepository.findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of());

        final String searchStr = "test";

        SortedSet<TopicThreadViewDto> results = topicThreadService.searchTopicThreads(TEST_TOPIC_FORUM_NAME, searchStr);

        assertEquals(0, results.size());

        verify(topicThreadRepository, times(1))
                .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchTopicThreads_emptySearch() throws Exception {
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        final String searchStr = "";

        SortedSet<TopicThreadViewDto> results = topicThreadService.searchTopicThreads(TEST_TOPIC_FORUM_NAME, searchStr);

        assertEquals(1, results.size());

        verify(topicForumRepository, times(1)).findByName(anyString());
        verify(topicThreadRepository, times(0))
                .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString());
    }

    @Test
    void canUserLockThread_valid() throws Exception {
        boolean result = topicThreadService.canUserLockThread(testModerator, testTopicThread);

        assertTrue(result);
    }

    @Test
    void canUserLockThread_nullUserOrThread() throws Exception {
        boolean result = topicThreadService.canUserLockThread(null, null);

        assertFalse(result);
    }

    @Test
    void canUserLockThread_alreadyLocked() throws Exception {
        testTopicThread.lock(testModerator);
        boolean result = topicThreadService.canUserLockThread(testAdmin, testTopicThread);

        assertFalse(result);
    }

    @Test
    void canUserLockThread_sameRankAsThreadCreator() throws Exception {
        testPost.setUser(testModerator2);

        boolean result = topicThreadService.canUserLockThread(testModerator, testTopicThread);

        assertFalse(result);
    }

    @Test
    void canUserLockThread_insufficientRank() throws Exception {
        boolean result = topicThreadService.canUserLockThread(testUser, testTopicThread);

        assertFalse(result);
    }

    @Test
    void canUserUnlockThread_valid() throws Exception {
        testTopicThread.lock(testModerator);

        boolean result = topicThreadService.canUserUnlockThread(testAdmin, testTopicThread);

        assertTrue(result);
    }

    @Test
    void canUserUnlockThread_nullUserOrThread() throws Exception {
        boolean result = topicThreadService.canUserUnlockThread(null, null);

        assertFalse(result);
    }

    @Test
    void canUserUnlockThread_alreadyUnlocked() throws Exception {
        boolean result = topicThreadService.canUserUnlockThread(testModerator, testTopicThread);

        assertFalse(result);
    }

    @Test
    void canUserUnlockThread_sameRankAsThreadLocker() throws Exception {
        testTopicThread.lock(testModerator2);

        boolean result = topicThreadService.canUserUnlockThread(testModerator, testTopicThread);

        assertFalse(result);
    }

    @Test
    void canUserUnlockThread_insufficientRank() throws Exception {
        testTopicThread.lock(testModerator);

        boolean result = topicThreadService.canUserUnlockThread(testUser, testTopicThread);

        assertFalse(result);
    }

    @Test
    void lockThread_valid() throws Exception {
        boolean result = topicThreadService.lockThread(testAdmin, testTopicThread);

        assertTrue(result);
        assertTrue(testTopicThread.isLocked());

        verify(topicThreadRepository, times(1)).save(any());
    }

    @Test
    void lockThread_invalid() throws Exception {
        boolean result = topicThreadService.lockThread(testUser, testTopicThread);

        assertFalse(result);
        assertFalse(testTopicThread.isLocked());

        verify(topicThreadRepository, times(0)).save(any());
    }

    @Test
    void unlockThread_valid() throws Exception {
        testTopicThread.lock(testModerator);

        boolean result = topicThreadService.unlockThread(testModerator, testTopicThread);

        assertTrue(result);
        assertTrue(!testTopicThread.isLocked());

        verify(topicThreadRepository, times(1)).save(any());
    }

    @Test
    void unlockThread_invalid() throws Exception {
        testTopicThread.lock(testAdmin);

        boolean result = topicThreadService.unlockThread(testModerator, testTopicThread);

        assertFalse(result);
        assertFalse(!testTopicThread.isLocked());

        verify(topicThreadRepository, times(0)).save(any());
    }

    @Test
    void getPostPage_valid() throws Exception {
        Page<Post> postPage = new PageImpl<Post>(testTopicThread.getPosts().stream().collect(Collectors.toList()));
        when(postRepository.findAllByThread(any(), any())).thenReturn(postPage);

        Page<Post> result = topicThreadService.getPostPageByThread(testTopicThread, 1, 25);

        assertEquals(postPage.getNumberOfElements(), result.getNumberOfElements());

        verify(postRepository, times(1)).findAllByThread(any(), any());
    }

    @Test
    void getPostPage_negativePageNumber() throws Exception {
        Page<Post> postPage = new PageImpl<Post>(testTopicThread.getPosts().stream().collect(Collectors.toList()));
        when(postRepository.findAllByThread(any(), any())).thenReturn(postPage);

        Page<Post> result = topicThreadService.getPostPageByThread(testTopicThread, -1, 25);

        assertNull(result);

        verify(postRepository, times(0)).findAllByThread(any(), any());
    }

    @Test
    void getPostPage_abovePageCountPageNumber() throws Exception {
        Page<Post> postPage = new PageImpl<Post>(testTopicThread.getPosts().stream().collect(Collectors.toList()));
        when(postRepository.findAllByThread(any(), any())).thenReturn(postPage);

        Page<Post> result = topicThreadService.getPostPageByThread(testTopicThread, 2, 25);

        assertNull(result);

        verify(postRepository, times(1)).findAllByThread(any(), any());

    }

    @Test
    void getTopicThreadsByForumPaginated_valid_withThreads() throws Exception {
        Page<TopicThread> expectedThreads = new PageImpl<TopicThread>(List.of(testTopicThread));

        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(expectedThreads);

        Page<TopicThread> resultThreads = topicThreadService.getTopicThreadsByForumPaginated(testTopicForum, 1, 25);

        assertEquals(expectedThreads.getTotalPages(), resultThreads.getTotalPages());
        assertEquals(expectedThreads.getTotalElements(), resultThreads.getTotalElements());
        assertEquals(expectedThreads.getNumber(), resultThreads.getNumber());
        assertEquals(expectedThreads.getNumberOfElements(), resultThreads.getNumberOfElements());
        assertEquals(expectedThreads.getContent().get(0), resultThreads.getContent().get(0));
    }

    @Test
    void getTopicThreadsByForumPaginated_valid_noThreads() throws Exception {
        Page<TopicThread> expectedThreads = new PageImpl<TopicThread>(new ArrayList<TopicThread>());

        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(expectedThreads);

        Page<TopicThread> resultThreads = topicThreadService.getTopicThreadsByForumPaginated(testTopicForum, 1, 25);

        assertEquals(expectedThreads.getTotalPages(), resultThreads.getTotalPages());
        assertEquals(0, resultThreads.getTotalElements());
        assertEquals(0, resultThreads.getNumber());
        assertEquals(0, resultThreads.getNumberOfElements());
        assertTrue(resultThreads.isEmpty());
    }

    @Test
    void getTopicThreadsByForumPaginated_lowPageNumber() throws Exception {
        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(null);

        Page<TopicThread> resultThreads = topicThreadService.getTopicThreadsByForumPaginated(testTopicForum, 0, 25);

        assertNull(resultThreads);

        verify(topicThreadRepository, times(0)).getAllTopicThreadsPaginated(anyString(), any());
    }

    @Test
    void getTopicThreadsByForumPaginated_highPageNumber() throws Exception {
        Page<TopicThread> expectedThreads = new PageImpl<TopicThread>(List.of(testTopicThread));

        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(expectedThreads);

        Page<TopicThread> resultThreads = topicThreadService.getTopicThreadsByForumPaginated(testTopicForum, 2, 25);

        assertNull(resultThreads);
        verify(topicThreadRepository, times(1)).getAllTopicThreadsPaginated(anyString(), any());
    }

    @Test
    void getTopicThreadViewDtosLightByForumPaginated_valid_withThreads() throws Exception {
        Page<TopicThread> expectedThreads = new PageImpl<TopicThread>(List.of(testTopicThread));

        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(expectedThreads);
        when(timeCalculatorService.getTimeSinceThreadCreationMessage(any())).thenReturn("3 days");
        when(timeCalculatorService.getTimeSinceThreadUpdatedMessage(any())).thenReturn("3 days");

        Page<TopicThreadViewDtoLight> resultThreads =
                topicThreadService.getTopicThreadViewDtosLightByForumPaginated(testTopicForum, 1,
                        25);

        assertEquals(1, resultThreads.getTotalPages());
        assertEquals(1, resultThreads.getTotalElements());
        assertEquals(0, resultThreads.getNumber());
        assertEquals(1, resultThreads.getNumberOfElements());
        assertEquals(testTopicThread.getId(), resultThreads.getContent().get(0).getId());
    }

    @Test
    void getTopicThreadViewDtosLightByForumPaginated_valid_noThreads() throws Exception {
        Page<TopicThread> expectedThreads = new PageImpl<TopicThread>(new ArrayList<TopicThread>());

        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(expectedThreads);

        Page<TopicThreadViewDtoLight> resultThreads =
                topicThreadService.getTopicThreadViewDtosLightByForumPaginated(testTopicForum, 1,
                        25);

        assertEquals(expectedThreads.getTotalPages(), resultThreads.getTotalPages());
        assertEquals(0, resultThreads.getTotalElements());
        assertEquals(0, resultThreads.getNumber());
        assertEquals(0, resultThreads.getNumberOfElements());
        assertTrue(resultThreads.isEmpty());
    }

    @Test
    void getTopicThreadViewDtosLightByForumPaginated_lowPageNumber() throws Exception {
        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(null);

        Page<TopicThreadViewDtoLight> resultThreads =
                topicThreadService.getTopicThreadViewDtosLightByForumPaginated(testTopicForum, 0,
                        25);

        assertNull(resultThreads);

        verify(topicThreadRepository, times(0)).getAllTopicThreadsPaginated(anyString(), any());
    }

    @Test
    void getTopicThreadViewDtosLightByForumPaginated__highPageNumber() throws Exception {
        Page<TopicThread> expectedThreads = new PageImpl<TopicThread>(List.of(testTopicThread));

        when(topicThreadRepository.getAllTopicThreadsPaginated(anyString(), any())).thenReturn(expectedThreads);

        Page<TopicThreadViewDtoLight> resultThreads =
                topicThreadService.getTopicThreadViewDtosLightByForumPaginated(testTopicForum, 2,
                        25);

        assertNull(resultThreads);
        verify(topicThreadRepository, times(1)).getAllTopicThreadsPaginated(anyString(), any());
    }

    @Test
    void searchTopicThreadsPaginated_valid_withResults() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsPaginated_valid_noResults() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsPaginated_invalid_lowPageNumber() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsPaginated_invalid_highPageNumber() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsAsViewDtos_valid_withResults() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsAsViewDtos_valid_noResults() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsAsViewDtos_invalid_lowPageNumber() throws Exception {
        // TODO: Fill in
    }

    @Test
    void searchTopicThreadsAsViewDtos_invalid_highPageNumber() throws Exception {
        // TODO: Fill in
    }
}