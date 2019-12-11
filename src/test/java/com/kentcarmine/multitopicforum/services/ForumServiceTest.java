package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
class ForumServiceTest {
    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumService = new ForumServiceImpl(topicForumRepository, topicForumDtoToTopicForumConverter, topicThreadRepository, postRepository);

        testTopicForum = new TopicForum(TEST_TOPIC_FORUM_NAME, TEST_TOPIC_FORUM_DESC);
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
}