package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDtoLight;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDtoLight;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.ForumService;
import com.kentcarmine.multitopicforum.services.MessageService;
import com.kentcarmine.multitopicforum.services.TopicThreadService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class TopicForumControllerTest {
    private static final String TEST_TOPIC_FORUM_NAME = "TestName";
    private static final String TEST_TOPIC_FORUM_DESC = "Description of test topic forum";

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

    TopicForumController topicForumController;

    MockMvc mockMvc;

    @Mock
    ForumService forumService;

    @Mock
    TopicThreadService topicThreadService;

    @Mock
    MessageService messageService;

    ForumHierarchyConverter forumHierarchyConverter;

    TopicForum testTopicForum;
    User testUser;
    private User testModerator;
    private User testModerator2;
    private User testAdmin;
    private User testSuperAdmin;
    TopicThread testTopicForumThread;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        forumHierarchyConverter = new ForumHierarchyConverter();

        topicForumController = new TopicForumController(forumService, topicThreadService);

        mockMvc = MockMvcBuilders.standaloneSetup(topicForumController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

        testModerator = new User(TEST_MODERATOR_USERNAME, TEST_MODERATOR_PASSWORD, TEST_MODERATOR_EMAIL);
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testModerator2 = new User(TEST_MODERATOR_2_USERNAME, TEST_MODERATOR_2_PASSWORD, TEST_MODERATOR_2_EMAIL);
        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);

        testSuperAdmin = new User(TEST_SUPER_ADMIN_USERNAME, TEST_SUPER_ADMIN_PASSWORD, TEST_SUPER_ADMIN_EMAIL);
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);

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
        TopicForumViewDtoLight topicForumDto = forumHierarchyConverter.convertForumLight(testTopicForum);

        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(forumService.getTopicForumViewDtoLightForTopicForum(any())).thenReturn(topicForumDto);

        TopicThreadViewDtoLight threadDto = forumHierarchyConverter.convertThreadLight(testTopicForumThread, topicForumDto);
        List<TopicThreadViewDtoLight> threadsContent = new ArrayList<>();
        threadsContent.add(threadDto);
        Pageable pageReq = PageRequest.of(0, 1);
        Page<TopicThreadViewDtoLight> threadPage = new PageImpl<TopicThreadViewDtoLight>(threadsContent, pageReq, threadsContent.size());

        when(topicThreadService.getTopicThreadViewDtosLightByForumPaginated(any(), anyInt(), anyInt())).thenReturn(threadPage);

        mockMvc.perform(get("/forum/" + testTopicForum.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("forum-page"))
                .andExpect(model().attributeExists("forum", "topicThreadSearchDto", "threads"));
    }

    @Test
    void showForum_nonExistingForum() throws Exception {
        when(forumService.getForumByName(anyString())).thenReturn(null);
        when(messageService.getMessage(eq("Exception.forum.notfound"))).thenReturn("Forum was not found.");

        mockMvc.perform(get("/forum/" + testTopicForum.getName()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));
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
        List<TopicForumViewDto> forumList = new ArrayList<>();
        forumList.add(forumHierarchyConverter.convertForum(testTopicForum));

        when(forumService.getForumsAsViewDtosPaginated(anyInt(), anyInt())).thenReturn(new PageImpl<TopicForumViewDto>(forumList));

        mockMvc.perform(get("/forums"))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(1)).getForumsAsViewDtosPaginated(anyInt(), anyInt());
//        verify(forumService, times(0)).searchTopicForumsForViewDtos(anyString());
        verify(forumService, times(0)).searchTopicForumsForViewDtosWithCustomQuery(anyString(), anyInt(), anyInt());
    }

    @Test
    void showForumsPage_validForumSearch() throws Exception {
        String searchString = URLEncoderDecoderHelper.encode(" \"Description of test \"   ");
//        SortedSet<TopicForum> forumsResults = new TreeSet<>(new Comparator<TopicForum>() {
//            @Override
//            public int compare(TopicForum o1, TopicForum o2) {
//                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
//            }
//        });
//        forumsResults.add(testTopicForum);

        List<TopicForumViewDto> forumList = new ArrayList<>();
        forumList.add(forumHierarchyConverter.convertForum(testTopicForum));
        PageRequest pageReq = PageRequest.of(0, 25);

        Page<TopicForumViewDto> resPage = new PageImpl<TopicForumViewDto>(forumList, pageReq,1);

//        when(forumService.searchTopicForums(anyString())).thenReturn(forumsResults);
//        when(forumService.searchTopicForumsForViewDtosPaginated(anyString(), anyInt())).thenReturn(resPage);
        when(forumService.searchTopicForumsForViewDtosWithCustomQuery(anyString(), anyInt(), anyInt())).thenReturn(resPage);

        mockMvc.perform(get("/forums?search=" + searchString))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

//        verify(forumService, times(0)).getAllForumsAsViewDtos();
        verify(forumService, times(0)).getForumsAsViewDtosPaginated(anyInt(), anyInt());
//        verify(forumService, times(1)).searchTopicForumsForViewDtos(anyString());
//        verify(forumService, times(1)).searchTopicForumsForViewDtosPaginated(anyString(), anyInt());
        verify(forumService, times(1)).searchTopicForumsForViewDtosWithCustomQuery(anyString(), anyInt(), anyInt());
    }

    @Test
    void showForumsPage_invalidForumSearch() throws Exception {
        when(forumService.getForumsAsViewDtosPaginated(anyInt(), anyInt())).thenReturn(new PageImpl<TopicForumViewDto>(new ArrayList<>()));

        mockMvc.perform(get("/forums?searchError"))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(1)).getForumsAsViewDtosPaginated(anyInt(), anyInt());
//        verify(forumService, times(0)).searchTopicForumsForViewDtos(anyString());
        verify(forumService, times(0)).searchTopicForumsForViewDtosWithCustomQuery(anyString(), anyInt(), anyInt());
    }

    @Test
    void showForumsPage_emptyStringForumSearch() throws Exception {
        List<TopicForumViewDto> forumList = new ArrayList<>();
        forumList.add(forumHierarchyConverter.convertForum(testTopicForum));

        when(forumService.getForumsAsViewDtosPaginated(anyInt(), anyInt())).thenReturn(new PageImpl<TopicForumViewDto>(forumList));

        mockMvc.perform(get("/forums?search="))
                .andExpect(status().isOk())
                .andExpect(view().name("forums-list-page"))
                .andExpect(model().attributeExists("forums"))
                .andExpect(model().attributeExists("topicForumSearchDto"));

        verify(forumService, times(1)).getForumsAsViewDtosPaginated(anyInt(), anyInt());
//        verify(forumService, times(0)).searchTopicForumsForViewDtos(anyString());
        verify(forumService, times(0)).searchTopicForumsForViewDtosWithCustomQuery(anyString(), anyInt(), anyInt());
    }

}