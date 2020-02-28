package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
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

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
class ForumServiceTest {
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

    ForumService forumService;

    @Mock
    TopicForumRepository topicForumRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    TopicThreadRepository topicThreadRepository;

    @Mock
    TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;

    @Mock
    PostVoteRepository postVoteRepository;

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

        forumService = new ForumServiceImpl(topicForumRepository, topicForumDtoToTopicForumConverter);

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

        testTopicForum2 = new TopicForum(TEST_TOPIC_FORUM_NAME_2, TEST_TOPIC_FORUM_DESC_2);
        testTopicThread2 = new TopicThread(TEST_TOPIC_THREAD_NAME_2, testTopicForum2);
    }

    @Test
    void getForumByName_existingForum() throws Exception {
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        TopicForum result = forumService.getForumByName(testTopicForum.getName());

        assertEquals(testTopicForum, result);
        verify(topicForumRepository, times(1)).findByName(anyString());
    }

    @Test
    void getForumByName_nonExistingForum() throws Exception {
        when(topicForumRepository.findByName(anyString())).thenReturn(null);

        TopicForum result = forumService.getForumByName(testTopicForum.getName());

        assertNull(result);
        verify(topicForumRepository, times(1)).findByName(anyString());
    }

    @Test
    void isForumWithNameExists_existingForum() throws Exception {
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        boolean result = forumService.isForumWithNameExists(testTopicForum.getName());

        assertTrue(result);
    }

    @Test
    void isForumWithNameExists_nonExistingForum() throws Exception {
        when(topicForumRepository.findByName(anyString())).thenReturn(null);

        boolean result = forumService.isForumWithNameExists(testTopicForum.getName());

        assertFalse(result);
    }

    @Test
    void createForumByDto_nonExistingForum() throws Exception {
        when(topicForumDtoToTopicForumConverter.convert(any())).thenReturn(testTopicForum);
        when(topicForumRepository.save(any())).thenReturn(testTopicForum);

        TopicForumDto forumDto = new TopicForumDto(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);

        TopicForum result = forumService.createForumByDto(forumDto);

        assertEquals(testTopicForum, result);

        verify(topicForumDtoToTopicForumConverter, times(1)).convert(any());
        verify(topicForumRepository, times(1)).save(any());
    }

    @Test
    void createForumByDto_existingForum() throws Exception {
        when(topicForumDtoToTopicForumConverter.convert(any())).thenReturn(testTopicForum);
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        TopicForumDto forumDto = new TopicForumDto(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);

        assertThrows(DuplicateForumNameException.class, () -> forumService.createForumByDto(forumDto));

        verify(topicForumDtoToTopicForumConverter, times(1)).convert(any());
        verify(topicForumRepository, times(1)).findByName(anyString());
        verify(topicForumRepository, times(0)).save(any());
    }

    @Test
    void createForum_nonExistingForum() throws Exception {
        when(topicForumRepository.save(any())).thenReturn(testTopicForum);

        TopicForum result = forumService.createForum(testTopicForum);

        assertEquals(testTopicForum, result);

        verify(topicForumRepository, times(1)).save(any());
    }

//    @Test
//    void createNewTopicThread() throws Exception {
//        final String postContent = "test post content";
//        User user = new User("TestUser", "testPassword", "test@test.com");
//        user.addAuthority(UserRole.USER);
//        TopicThreadCreationDto thread = new TopicThreadCreationDto(TEST_TOPIC_THREAD_NAME, postContent);
//        forumService.createNewTopicThread(thread, user, testTopicForum);
//
//        verify(topicThreadRepository, times(1)).save(any());
//        verify(postRepository, times(1)).save(any());
//    }

//    @Test
//    void getThreadByForumNameAndId_valid() throws Exception {
//        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread));
//        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
//
//        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);
//
//        assertNotNull(result);
//        assertEquals(testTopicThread, result);
//    }
//
//    @Test
//    void getThreadByForumNameAndId_threadDoesNotBelongToForum() throws Exception {
//        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread2));
//        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
//
//        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);
//
//        assertNull(result);
//    }
//
//    @Test
//    void getThreadByForumNameAndId_threadDoesNotExist() throws Exception {
//        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.empty());
//        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
//
//        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);
//
//        assertNull(result);
//    }
//
//    @Test
//    void getThreadByForumNameAndId_forumDoesNotExist() throws Exception {
//        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread));
//        when(topicForumRepository.findByName(anyString())).thenReturn(null);
//
//        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);
//
//        assertNull(result);
//    }

//    @Test
//    void addNewPostToThread() throws Exception {
//        final String testContent = "test content";
//        PostCreationDto postCreationDto = new PostCreationDto(testContent);
//        final User testUser = new User("testUser", "password", "test@test.com");
//        testUser.addAuthority(UserRole.USER);
//
//        forumService.addNewPostToThread(postCreationDto, testUser, testTopicThread);
//
//        verify(postRepository, times(1)).save(any());
//    }

    @Test
    void searchTopicForums_multipleResults() throws Exception {
        when(topicForumRepository.findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(anyString(), anyString())).thenReturn(List.of(testTopicForum, testTopicForum2));

        final String searchStr = "\"Description of test\"";

        SortedSet<TopicForum> results = forumService.searchTopicForums(searchStr);

        assertEquals(2, results.size());
        assertEquals(testTopicForum, results.first());
        assertEquals(testTopicForum2, results.last());

        verify(topicForumRepository, times(1)).findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchTopicForums_noResults() throws Exception {
        when(topicForumRepository.findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(anyString(), anyString())).thenReturn(new ArrayList<TopicForum>());

        final String searchStr = "\"foo BAR baz\"";

        SortedSet<TopicForum> results = forumService.searchTopicForums(searchStr);

        assertEquals(0, results.size());

        verify(topicForumRepository, times(1)).findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchTopicForums_emptySearch() throws Exception {
        final String searchStr = "";

        SortedSet<TopicForum> results = forumService.searchTopicForums(searchStr);

        assertEquals(0, results.size());

        verify(topicForumRepository, times(0)).findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(anyString(), anyString());
    }

//    @Test
//    void searchTopicThreads_existingResults() throws Exception {
//        when(topicThreadRepository.findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString()))
//                .thenReturn(List.of(testTopicThread));
//
//        final String searchStr = "test";
//
//        SortedSet<TopicThread> results = forumService.searchTopicThreads(TEST_TOPIC_FORUM_NAME, searchStr);
//
//        assertEquals(1, results.size());
//        assertEquals(testTopicThread.getTitle(), results.first().getTitle());
//
//        verify(topicForumRepository, times(0)).findByName(anyString());
//        verify(topicThreadRepository, times(1))
//                .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString());
//    }
//
//    @Test
//    void searchTopicThreads_noResults() throws Exception {
//        when(topicThreadRepository.findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString()))
//                .thenReturn(List.of());
//
//        final String searchStr = "test";
//
//        SortedSet<TopicThread> results = forumService.searchTopicThreads(TEST_TOPIC_FORUM_NAME, searchStr);
//
//        assertEquals(0, results.size());
//
//        verify(topicForumRepository, times(0)).findByName(anyString());
//        verify(topicThreadRepository, times(1))
//                .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString());
//    }
//
//    @Test
//    void searchTopicThreads_emptySearch() throws Exception {
//        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);
//
//        final String searchStr = "";
//
//        SortedSet<TopicThread> results = forumService.searchTopicThreads(TEST_TOPIC_FORUM_NAME, searchStr);
//
//        assertEquals(1, results.size());
//
//        verify(topicForumRepository, times(1)).findByName(anyString());
//        verify(topicThreadRepository, times(0))
//                .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(anyString(), anyString());
//    }

//    @Test
//    void handlePostVoteSubmission_validUpvote() throws Exception {
//        PostVote postVote = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
//        postVote.setId(3L);
//        PostVoteSubmissionDto submissionDto = new PostVoteSubmissionDto(postVote.getPost().getId(), postVote.getPostVoteState().getValue());
//
//        when(postVoteRepository.findByUserAndPost(any(), any())).thenReturn(null);
//        when(postVoteRepository.save(any())).thenReturn(postVote);
//
//        assertEquals(0, testPost.getPostVotes().size());
//        assertEquals(0, testPost.getVoteCount());
//        PostVoteResponseDto response = forumService.handlePostVoteSubmission(testUser, testPost, submissionDto);
//
//        assertEquals(1, testPost.getPostVotes().size());
//        assertEquals(PostVoteState.UPVOTE, testPost.getPostVotes().iterator().next().getPostVoteState());
//        assertEquals(testPost.getPostVotes().iterator().next().getPost().getId(), response.getPostId());
//        assertEquals(1, testPost.getVoteCount());
//        assertTrue(response.isVoteUpdated());
//        assertTrue(response.isHasUpvote());
//        assertFalse(response.isHasDownvote());
//        assertEquals(1, testPost.getVoteCount());
//        assertEquals(1, response.getVoteTotal());
//        assertEquals(testPost.getVoteCount(), response.getVoteTotal());
//
//        verify(postVoteRepository, times(1)).findByUserAndPost(any(), any());
//        verify(postVoteRepository, times(1)).save(any());
//    }
//
//    @Test
//    void handlePostVoteSubmission_validDownvote() throws Exception {
//        PostVote postVote = new PostVote(PostVoteState.DOWNVOTE, testUser, testPost);
//        postVote.setId(3L);
//        PostVoteSubmissionDto submissionDto = new PostVoteSubmissionDto(postVote.getPost().getId(), postVote.getPostVoteState().getValue());
//
//        when(postVoteRepository.findByUserAndPost(any(), any())).thenReturn(null);
//        when(postVoteRepository.save(any())).thenReturn(postVote);
//
//        assertEquals(0, testPost.getPostVotes().size());
//        assertEquals(0, testPost.getVoteCount());
//
//        PostVoteResponseDto response = forumService.handlePostVoteSubmission(testUser, testPost, submissionDto);
//
//        assertEquals(1, testPost.getPostVotes().size());
//        assertEquals(PostVoteState.DOWNVOTE, testPost.getPostVotes().iterator().next().getPostVoteState());
//        assertEquals(testPost.getPostVotes().iterator().next().getPost().getId(), response.getPostId());
//        assertTrue(response.isVoteUpdated());
//        assertFalse(response.isHasUpvote());
//        assertTrue(response.isHasDownvote());
//        assertEquals(-1, testPost.getVoteCount());
//        assertEquals(-1, response.getVoteTotal());
//        assertEquals(testPost.getVoteCount(), response.getVoteTotal());
//
//        verify(postVoteRepository, times(1)).findByUserAndPost(any(), any());
//        verify(postVoteRepository, times(1)).save(any());
//    }
//
//    @Test
//    void handlePostVoteSubmission_invalidVote() throws Exception {
//        PostVote postVote = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
//        postVote.setId(3L);
//        testPost.addPostVote(postVote);
//        testUser.getPostVotes().add(postVote);
//
//        PostVoteSubmissionDto submissionDto = new PostVoteSubmissionDto(postVote.getPost().getId(), postVote.getPostVoteState().getValue());
//
//        when(postVoteRepository.findByUserAndPost(any(), any())).thenReturn(postVote);
//
//        assertEquals(1, testPost.getPostVotes().size());
//        assertEquals(1, testPost.getVoteCount());
//
//        PostVoteResponseDto response = forumService.handlePostVoteSubmission(testUser, testPost, submissionDto);
//
//        assertEquals(1, testPost.getPostVotes().size());
//        assertEquals(PostVoteState.UPVOTE, testPost.getPostVotes().iterator().next().getPostVoteState());
//        assertEquals(testPost.getPostVotes().iterator().next().getPost().getId(), response.getPostId());
//        assertFalse(response.isVoteUpdated());
//        assertTrue(response.isHasUpvote());
//        assertFalse(response.isHasDownvote());
//        assertEquals(1, testPost.getVoteCount());
//        assertEquals(1, response.getVoteTotal());
//        assertEquals(testPost.getVoteCount(), response.getVoteTotal());
//
//        verify(postVoteRepository, times(1)).findByUserAndPost(any(), any());
//        verify(postVoteRepository, times(0)).save(any());
//    }

//    @Test
//    void deletePost() throws Exception {
//        assertFalse(testPost.isDeleted());
//
//        Date deletedAtTimestamp = Date.from(Instant.now());
//        User deletingUser = testModerator;
//
//        Post testPostDeleted = new Post(testPost.getContent(), testPost.getPostedAt());
//        testPostDeleted.setId(testPost.getId());
//        testPostDeleted.setThread(testPost.getThread());
//        testPostDeleted.setUser(testPost.getUser());
//        testPostDeleted.setPostVotes(testPost.getPostVotes());
//        testPostDeleted.setDeleted(true);
//        testPostDeleted.setDeletedBy(deletingUser);
//        testPostDeleted.setDeletedAt(deletedAtTimestamp);
//
//        when(postRepository.save(any())).thenReturn(testPostDeleted);
//
//        Post result = forumService.deletePost(testPost, deletingUser);
//
//        assertEquals(testPost.getId(), result.getId());
//        assertEquals(testPost.getContent(), result.getContent());
//        assertEquals(testPost.getPostedAt(), result.getPostedAt());
//        assertEquals(testPost.getThread(), result.getThread());
//        assertEquals(testPost.getUser(), result.getUser());
//        assertTrue(result.isDeleted());
//        assertEquals(deletingUser, result.getDeletedBy());
//        assertEquals(deletedAtTimestamp, result.getDeletedAt());
//
//        verify(postRepository, times(1)).save(any());
//    }
//
//    @Test
//    void deletePost_alreadyDeleted() throws Exception {
//        testPost.setDeleted(true);
//
//        forumService.deletePost(testPost, testModerator);
//
//        verify(postRepository, times(0)).save(any());
//    }

//    @Test
//    void restorePost() throws Exception {
//        Date deletedAtTimestamp =  Date.from(Instant.now());
//        User deletingUser = testModerator;
//        testPost.setDeleted(true);
//        testPost.setDeletedAt(deletedAtTimestamp);
//        testPost.setDeletedBy(deletingUser);
//
//        Post testPostRestored = new Post(testPost.getContent(), testPost.getPostedAt());
//        testPostRestored.setId(testPost.getId());
//        testPostRestored.setThread(testPost.getThread());
//        testPostRestored.setUser(testPost.getUser());
//        testPostRestored.setPostVotes(testPost.getPostVotes());
//        testPostRestored.setDeleted(false);
//        testPostRestored.setDeletedBy(null);
//        testPostRestored.setDeletedAt(null);
//
//        when(postRepository.save(any())).thenReturn(testPostRestored);
//
//        Post result = forumService.restorePost(testPost);
//
//        assertEquals(testPost.getId(), result.getId());
//        assertEquals(testPost.getContent(), result.getContent());
//        assertEquals(testPost.getPostedAt(), result.getPostedAt());
//        assertEquals(testPost.getThread(), result.getThread());
//        assertEquals(testPost.getUser(), result.getUser());
//        assertFalse(result.isDeleted());
//        assertNull(result.getDeletedBy());
//        assertNull(result.getDeletedAt());
//
//        verify(postRepository, times(1)).save(any());
//    }

//    @Test
//    void canUserLockThread_valid() throws Exception {
//        boolean result = forumService.canUserLockThread(testModerator, testTopicThread);
//
//        assertTrue(result);
//    }
//
//    @Test
//    void canUserLockThread_nullUserOrThread() throws Exception {
//        boolean result = forumService.canUserLockThread(null, null);
//
//        assertFalse(result);
//    }
//
//    @Test
//    void canUserLockThread_alreadyLocked() throws Exception {
//        testTopicThread.lock(testModerator);
//        boolean result = forumService.canUserLockThread(testAdmin, testTopicThread);
//
//        assertFalse(result);
//    }
//
//    @Test
//    void canUserLockThread_sameRankAsThreadCreator() throws Exception {
//        testPost.setUser(testModerator2);
//
//        boolean result = forumService.canUserLockThread(testModerator, testTopicThread);
//
//        assertFalse(result);
//    }
//
//    @Test
//    void canUserLockThread_insufficientRank() throws Exception {
//        boolean result = forumService.canUserLockThread(testUser, testTopicThread);
//
//        assertFalse(result);
//    }

//    @Test
//    void canUserUnlockThread_valid() throws Exception {
//        testTopicThread.lock(testModerator);
//
//        boolean result = forumService.canUserUnlockThread(testAdmin, testTopicThread);
//
//        assertTrue(result);
//    }
//
//    @Test
//    void canUserUnlockThread_nullUserOrThread() throws Exception {
//        boolean result = forumService.canUserUnlockThread(null, null);
//
//        assertFalse(result);
//    }
//
//    @Test
//    void canUserUnlockThread_alreadyUnlocked() throws Exception {
//        boolean result = forumService.canUserUnlockThread(testModerator, testTopicThread);
//
//        assertFalse(result);
//    }
//
//    @Test
//    void canUserUnlockThread_sameRankAsThreadLocker() throws Exception {
//        testTopicThread.lock(testModerator2);
//
//        boolean result = forumService.canUserUnlockThread(testModerator, testTopicThread);
//
//        assertFalse(result);
//    }
//
//    @Test
//    void canUserUnlockThread_insufficientRank() throws Exception {
//        testTopicThread.lock(testModerator);
//
//        boolean result = forumService.canUserUnlockThread(testUser, testTopicThread);
//
//        assertFalse(result);
//    }

//    @Test
//    void lockThread_valid() throws Exception {
//        boolean result = forumService.lockThread(testAdmin, testTopicThread);
//
//        assertTrue(result);
//        assertTrue(testTopicThread.isLocked());
//
//        verify(topicThreadRepository, times(1)).save(any());
//    }
//
//    @Test
//    void lockThread_invalid() throws Exception {
//        boolean result = forumService.lockThread(testUser, testTopicThread);
//
//        assertFalse(result);
//        assertFalse(testTopicThread.isLocked());
//
//        verify(topicThreadRepository, times(0)).save(any());
//    }

//    @Test
//    void unlockThread_valid() throws Exception {
//        testTopicThread.lock(testModerator);
//
//        boolean result = forumService.unlockThread(testModerator, testTopicThread);
//
//        assertTrue(result);
//        assertTrue(!testTopicThread.isLocked());
//
//        verify(topicThreadRepository, times(1)).save(any());
//    }
//
//    @Test
//    void unlockThread_invalid() throws Exception {
//        testTopicThread.lock(testAdmin);
//
//        boolean result = forumService.unlockThread(testModerator, testTopicThread);
//
//        assertFalse(result);
//        assertFalse(!testTopicThread.isLocked());
//
//        verify(topicThreadRepository, times(0)).save(any());
//    }

}