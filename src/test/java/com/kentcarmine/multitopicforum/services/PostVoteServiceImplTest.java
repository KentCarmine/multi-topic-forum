package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
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
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class PostVoteServiceImplTest {

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

    PostVoteService postVoteService;

    @Mock
    PostVoteRepository postVoteRepository;

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

        postVoteService = new PostVoteServiceImpl(postVoteRepository);

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
    void handlePostVoteSubmission_validUpvote() throws Exception {
        PostVote postVote = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
        postVote.setId(3L);
        PostVoteSubmissionDto submissionDto = new PostVoteSubmissionDto(postVote.getPost().getId(), postVote.getPostVoteState().getValue());

        when(postVoteRepository.findByUserAndPost(any(), any())).thenReturn(null);
        when(postVoteRepository.save(any())).thenReturn(postVote);

        assertEquals(0, testPost.getPostVotes().size());
        assertEquals(0, testPost.getVoteCount());
        PostVoteResponseDto response = postVoteService.handlePostVoteSubmission(testUser, testPost, submissionDto);

        assertEquals(1, testPost.getPostVotes().size());
        assertEquals(PostVoteState.UPVOTE, testPost.getPostVotes().iterator().next().getPostVoteState());
        assertEquals(testPost.getPostVotes().iterator().next().getPost().getId(), response.getPostId());
        assertEquals(1, testPost.getVoteCount());
        assertTrue(response.isVoteUpdated());
        assertTrue(response.isHasUpvote());
        assertFalse(response.isHasDownvote());
        assertEquals(1, testPost.getVoteCount());
        assertEquals(1, response.getVoteTotal());
        assertEquals(testPost.getVoteCount(), response.getVoteTotal());

        verify(postVoteRepository, times(1)).findByUserAndPost(any(), any());
        verify(postVoteRepository, times(1)).save(any());
    }

    @Test
    void handlePostVoteSubmission_validDownvote() throws Exception {
        PostVote postVote = new PostVote(PostVoteState.DOWNVOTE, testUser, testPost);
        postVote.setId(3L);
        PostVoteSubmissionDto submissionDto = new PostVoteSubmissionDto(postVote.getPost().getId(), postVote.getPostVoteState().getValue());

        when(postVoteRepository.findByUserAndPost(any(), any())).thenReturn(null);
        when(postVoteRepository.save(any())).thenReturn(postVote);

        assertEquals(0, testPost.getPostVotes().size());
        assertEquals(0, testPost.getVoteCount());

        PostVoteResponseDto response = postVoteService.handlePostVoteSubmission(testUser, testPost, submissionDto);

        assertEquals(1, testPost.getPostVotes().size());
        assertEquals(PostVoteState.DOWNVOTE, testPost.getPostVotes().iterator().next().getPostVoteState());
        assertEquals(testPost.getPostVotes().iterator().next().getPost().getId(), response.getPostId());
        assertTrue(response.isVoteUpdated());
        assertFalse(response.isHasUpvote());
        assertTrue(response.isHasDownvote());
        assertEquals(-1, testPost.getVoteCount());
        assertEquals(-1, response.getVoteTotal());
        assertEquals(testPost.getVoteCount(), response.getVoteTotal());

        verify(postVoteRepository, times(1)).findByUserAndPost(any(), any());
        verify(postVoteRepository, times(1)).save(any());
    }

    @Test
    void handlePostVoteSubmission_invalidVote() throws Exception {
        PostVote postVote = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
        postVote.setId(3L);
        testPost.addPostVote(postVote);
        testUser.getPostVotes().add(postVote);

        PostVoteSubmissionDto submissionDto = new PostVoteSubmissionDto(postVote.getPost().getId(), postVote.getPostVoteState().getValue());

        when(postVoteRepository.findByUserAndPost(any(), any())).thenReturn(postVote);

        assertEquals(1, testPost.getPostVotes().size());
        assertEquals(1, testPost.getVoteCount());

        PostVoteResponseDto response = postVoteService.handlePostVoteSubmission(testUser, testPost, submissionDto);

        assertEquals(1, testPost.getPostVotes().size());
        assertEquals(PostVoteState.UPVOTE, testPost.getPostVotes().iterator().next().getPostVoteState());
        assertEquals(testPost.getPostVotes().iterator().next().getPost().getId(), response.getPostId());
        assertFalse(response.isVoteUpdated());
        assertTrue(response.isHasUpvote());
        assertFalse(response.isHasDownvote());
        assertEquals(1, testPost.getVoteCount());
        assertEquals(1, response.getVoteTotal());
        assertEquals(testPost.getVoteCount(), response.getVoteTotal());

        verify(postVoteRepository, times(1)).findByUserAndPost(any(), any());
        verify(postVoteRepository, times(0)).save(any());
    }
}