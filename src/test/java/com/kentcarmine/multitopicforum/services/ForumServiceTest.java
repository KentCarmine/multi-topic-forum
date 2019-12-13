package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

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

    ForumService forumService;

    @Mock
    TopicForumRepository topicForumRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    TopicThreadRepository topicThreadRepository;

    @Mock
    TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;

    private TopicForum testTopicForum;
    private TopicForum testTopicForum2;
    private TopicThread testTopicThread;
    private TopicThread testTopicThread2;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumService = new ForumServiceImpl(topicForumRepository, topicForumDtoToTopicForumConverter, topicThreadRepository, postRepository);

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
        testTopicThread = new TopicThread(TEST_TOPIC_THREAD_NAME, testTopicForum);

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

    @Test
    void createNewTopicThread() throws Exception {
        final String postContent = "test post content";
        User user = new User("TestUser", "testPassword", "test@test.com");
        user.addAuthority(UserRole.USER);
        TopicThreadCreationDto thread = new TopicThreadCreationDto(TEST_TOPIC_THREAD_NAME, postContent);
        forumService.createNewTopicThread(thread, user, testTopicForum);

        verify(topicThreadRepository, times(1)).save(any());
        verify(postRepository, times(1)).save(any());
    }

    @Test
    void getThreadByForumNameAndId_valid() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread));
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNotNull(result);
        assertEquals(testTopicThread, result);
    }

    @Test
    void getThreadByForumNameAndId_threadDoesNotBelongToForum() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread2));
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNull(result);
    }

    @Test
    void getThreadByForumNameAndId_threadDoesNotExist() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(topicForumRepository.findByName(anyString())).thenReturn(testTopicForum);

        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNull(result);
    }

    @Test
    void getThreadByForumNameAndId_forumDoesNotExist() throws Exception {
        when(topicThreadRepository.findById(anyLong())).thenReturn(Optional.of(testTopicThread));
        when(topicForumRepository.findByName(anyString())).thenReturn(null);

        TopicThread result = forumService.getThreadByForumNameAndId(testTopicForum.getName(), 1l);

        assertNull(result);
    }

    @Test
    void addNewPostToThread() throws Exception {
        // TODO
    }


}