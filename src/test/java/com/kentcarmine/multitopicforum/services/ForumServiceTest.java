package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
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
    TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;

    ForumHierarchyConverter forumHierarchyConverter;

    @Mock
    TimeCalculatorService timeCalculatorService;

    private TopicForum testTopicForum;
    private TopicForum testTopicForum2;
    private TopicThread testTopicThread;
    private Post testPost;

    private User testUser;
    private User testModerator;
    private User testModerator2;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumHierarchyConverter = new ForumHierarchyConverter();
        forumService = new ForumServiceImpl(topicForumRepository, topicForumDtoToTopicForumConverter, forumHierarchyConverter, timeCalculatorService);

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
        testPost.setThread(testTopicThread);

        testTopicForum2 = new TopicForum(TEST_TOPIC_FORUM_NAME_2, TEST_TOPIC_FORUM_DESC_2);
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
    void searchTopicForumsWithCustomQuery_valid_withResults() throws Exception {
        List<TopicForum> resultList = new ArrayList<>();
        resultList.add(testTopicForum);
        resultList.add(testTopicForum2);
        resultList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        PageRequest pageReq = PageRequest.of(0, 2, Sort.by(Sort.Order.by("name").ignoreCase()).descending());
        Page<TopicForum> expectedPage = new PageImpl<TopicForum>(resultList, pageReq, resultList.size());

        when(topicForumRepository.searchTopicForumsPaginated(any(), any())).thenReturn(expectedPage);

        Page<TopicForum> result = forumService.searchTopicForumsWithCustomQuery("test", 1, 2);

        assertEquals(expectedPage.getNumberOfElements(), result.getNumberOfElements());
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        assertEquals(expectedPage.getTotalPages(), result.getTotalPages());
        assertEquals(expectedPage.getNumber(), result.getNumber());
        assertEquals(testTopicForum, result.getContent().get(0));
        assertEquals(testTopicForum2, result.getContent().get(1));

    }

    @Test
    void searchTopicForumsWithCustomQuery_valid_noResults() throws Exception {
        List<TopicForum> resultList = new ArrayList<>();

        PageRequest pageReq = PageRequest.of(0, 2, Sort.by(Sort.Order.by("name").ignoreCase()).descending());
        Page<TopicForum> expectedPage = new PageImpl<TopicForum>(resultList, pageReq, resultList.size());

        when(topicForumRepository.searchTopicForumsPaginated(any(), any())).thenReturn(expectedPage);

        Page<TopicForum> result = forumService.searchTopicForumsWithCustomQuery("test", 1, 2);

        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(0, result.getContent().size());

    }

    @Test
    void searchTopicForumsWithCustomQuery_invalid_lowPageNum() throws Exception {

        Page<TopicForum> result = forumService.searchTopicForumsWithCustomQuery("poasgog", 0, 2);

        assertNull(result);
    }

    @Test
    void searchTopicForumsWithCustomQuery_invalid_highPageNum() throws Exception {
        List<TopicForum> resultList = new ArrayList<>();
        resultList.add(testTopicForum);
        resultList.add(testTopicForum2);
        resultList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        PageRequest pageReq = PageRequest.of(0, 2, Sort.by(Sort.Order.by("name").ignoreCase()).descending());
        Page<TopicForum> expectedPage = new PageImpl<TopicForum>(resultList, pageReq, resultList.size());

        when(topicForumRepository.searchTopicForumsPaginated(any(), any())).thenReturn(expectedPage);

        Page<TopicForum> result = forumService.searchTopicForumsWithCustomQuery("tesasghashgt", 17, 2);

        assertNull(result);
    }

    @Test
    void getForumsAsViewDtosPaginated_valid() throws Exception {
        int resultsPerPage = 2;

        Pageable pageReq = PageRequest.of(0, resultsPerPage,
                Sort.by(Sort.Order.by("name").ignoreCase()).ascending());
        List<TopicForum> forumList = new ArrayList<>();
        forumList.add(testTopicForum);
        Page<TopicForum> forumPageExpected = new PageImpl<TopicForum>(forumList, pageReq, forumList.size());

        when(topicForumRepository.findAll(any(Pageable.class))).thenReturn(forumPageExpected);
        when(timeCalculatorService.getTimeSinceForumUpdatedMessage(any())).thenReturn("testPlaceholderText");

        Page<TopicForumViewDto> result = forumService.getForumsAsViewDtosPaginated(1, resultsPerPage);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getNumberOfElements());
        assertEquals(0, result.getNumber());

        TopicForumViewDto resultContent = result.toList().get(0);

        assertEquals(1, resultContent.getNumThreads());
        assertEquals(testTopicThread.getId(), resultContent.getThreads().first().getId());
        assertEquals(testTopicForum.getThreads().first().getPosts().first().getId(), resultContent.getMostRecentPost().getId());

        verify(topicForumRepository, times(1)).findAll(any(Pageable.class));
        verify(timeCalculatorService, times(1)).getTimeSinceForumUpdatedMessage(any());
    }

    @Test
    void getForumsAsViewDtosPaginated_lowPageNumber() throws Exception {
        int resultsPerPage = 25;

        Pageable pageReq = PageRequest.of(0, resultsPerPage,
                Sort.by(Sort.Order.by("name").ignoreCase()).ascending());
        List<TopicForum> forumList = new ArrayList<>();
        forumList.add(testTopicForum);
        forumList.add(testTopicForum2);
        Page<TopicForum> forumPageExpected = new PageImpl<TopicForum>(forumList, pageReq, 2);

        when(topicForumRepository.findAll(pageReq)).thenReturn(forumPageExpected);

        Page<TopicForumViewDto> result = forumService.getForumsAsViewDtosPaginated(0, resultsPerPage);

        assertNull(result);
    }

    @Test
    void getForumsAsViewDtosPaginated_highPageNumber() throws Exception {
        int resultsPerPage = 25;

        Pageable pageReq = PageRequest.of(0, resultsPerPage,
                Sort.by(Sort.Order.by("name").ignoreCase()).ascending());
        List<TopicForum> forumList = new ArrayList<>();
        forumList.add(testTopicForum);
        forumList.add(testTopicForum2);
        Page<TopicForum> forumPageExpected = new PageImpl<TopicForum>(forumList, pageReq, 2);

        when(topicForumRepository.findAll(any(Pageable.class))).thenReturn(forumPageExpected);

        Page<TopicForumViewDto> result = forumService.getForumsAsViewDtosPaginated(17, resultsPerPage);

        assertNull(result);
    }

    @Test
    void getTopicForumViewDtoLightForTopicForum_valid() throws Exception {
        TopicForumViewDtoLight expected = forumHierarchyConverter.convertForumLight(testTopicForum);

        when(timeCalculatorService.getTimeSinceThreadCreationMessage(any())).thenReturn("1 hour ago");
        when(timeCalculatorService.getTimeSinceThreadUpdatedMessage(any())).thenReturn("1 hour ago");
        when(timeCalculatorService.getTimeSincePostCreationMessage(any())).thenReturn("1 hour ago");

        TopicForumViewDtoLight result = forumService.getTopicForumViewDtoLightForTopicForum(testTopicForum);

        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertTrue(result.hasThreads());
        assertEquals(1, result.getNumThreads());
        assertEquals(expected.getMostRecentPost().getId(), result.getMostRecentPost().getId());

    }

    @Test
    void getTopicForumViewDtoLightForTopicForum_noPosts() throws Exception {
        TopicForumViewDtoLight expected = forumHierarchyConverter.convertForumLight(testTopicForum2);

        TopicForumViewDtoLight result = forumService.getTopicForumViewDtoLightForTopicForum(testTopicForum2);

        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertFalse(result.hasThreads());
        assertNull(result.getMostRecentPost());

        verify(timeCalculatorService, times(0)).getTimeSinceThreadCreationMessage(any());
        verify(timeCalculatorService, times(0)).getTimeSinceThreadUpdatedMessage(any());
        verify(timeCalculatorService, times(0)).getTimeSincePostCreationMessage(any());
    }




}