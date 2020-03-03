package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.*;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.time.Instant;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class TopicThreadControllerTest {
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

    TopicThreadController topicThreadController;

    MockMvc mockMvc;

    @Mock
    ForumService forumService;

    @Mock
    TopicThreadService topicThreadService;

    @Mock
    UserService userService;

    @Mock
    MessageService messageService;

    @Mock
    PostVoteService postVoteService;

    @Mock
    DisciplineService disciplineService;

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

        topicThreadController = new TopicThreadController(forumService, userService, topicThreadService,
                postVoteService, disciplineService);

        mockMvc = MockMvcBuilders.standaloneSetup(topicThreadController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

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
    void processSearchThreads_validSearch() throws Exception {
        String searchString = " \" Thread Title\"   ";

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/processSearchThreads/" + testTopicForum.getName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", searchString))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/searchForumThreads/" + testTopicForum.getName()
                        + "?search=" + URLEncoderDecoderHelper.encode(searchString.trim())));
    }

    @Test
    void processSearchThreads_invalidSearchText() throws Exception {
        String searchString = " \" invalid search text   ";

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/processSearchThreads/" + testTopicForum.getName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", searchString))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/searchForumThreads/" + testTopicForum.getName()
                        + "?searchError"));
    }

    @Test
    void processSearchThreads_noSuchForumName() throws Exception {
        String searchString = " \" Thread Title\"   ";

        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(messageService.getMessage(eq("Exception.forum.notfound"))).thenReturn("Forum was not found.");

        mockMvc.perform(post("/processSearchThreads/" + testTopicForum.getName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", searchString))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void searchForumThreads_validSearchWithResults() throws Exception {
        final String searchText = "test";
        final String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);
        final String url = "/searchForumThreads/" + testTopicForum.getName() + "?search=" + urlSafeSearchText;

        SortedSet<TopicThread> threadsResults  = new TreeSet<>(new Comparator<TopicThread>() {
            @Override
            public int compare(TopicThread o1, TopicThread o2) {
                return o2.getFirstPost().getPostedAt().compareTo(o1.getFirstPost().getPostedAt());
            }
        });
        threadsResults.add(testTopicForumThread);

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.searchTopicThreads(anyString(), anyString())).thenReturn(threadsResults);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("search-threads-results-page"))
                .andExpect(model().attributeExists("threads", "forumName", "searchText"))
                .andExpect(model().attribute("threads", IsCollectionWithSize.hasSize(threadsResults.size())));

        verify(forumService, times(1)).isForumWithNameExists(anyString());
        verify(topicThreadService, times(1)).searchTopicThreads(anyString(), anyString());
    }

    @Test
    void searchForumThreads_invalidSearch() throws Exception {
        final String url = "/searchForumThreads/" + testTopicForum.getName() + "?searchError";

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("search-threads-results-page"))
                .andExpect(model().attributeDoesNotExist("threads", "forumName", "searchText"));

        verify(forumService, times(1)).isForumWithNameExists(anyString());
        verify(topicThreadService, times(0)).searchTopicThreads(anyString(), anyString());
    }

    @Test
    void searchForumThreads_emptyStringSearch() throws Exception {
        final String searchText = "\"\"";
        final String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);
        final String url = "/searchForumThreads/" + testTopicForum.getName() + "?search=" + urlSafeSearchText;

        SortedSet<TopicThread> threadsResults  = new TreeSet<>(new Comparator<TopicThread>() {
            @Override
            public int compare(TopicThread o1, TopicThread o2) {
                return o2.getFirstPost().getPostedAt().compareTo(o1.getFirstPost().getPostedAt());
            }
        });
        threadsResults.add(testTopicForumThread);

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.searchTopicThreads(anyString(), anyString())).thenReturn(threadsResults);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("search-threads-results-page"))
                .andExpect(model().attributeExists("threads", "forumName", "searchText"))
                .andExpect(model().attribute("threads", IsCollectionWithSize.hasSize(threadsResults.size())));

        verify(forumService, times(1)).isForumWithNameExists(anyString());
        verify(topicThreadService, times(1)).searchTopicThreads(anyString(), anyString());
    }

    @Test
    void searchForumThreads_noSuchForumName() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(messageService.getMessage(eq("Exception.forum.notfound"))).thenReturn("Forum was not found.");

        mockMvc.perform(get("/searchForumThreads/aihgpnwng?searchError"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(forumService, times(1)).isForumWithNameExists(anyString());
        verify(topicThreadService, times(0)).searchTopicThreads(anyString(), anyString());
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
        when(topicThreadService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName()
                        + "/show/" + testTopicForumThread.getId()));
    }

    @Test
    void processCreateThread_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(disciplineService).handleDisciplinedUser(any());

        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);


        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));

        verify(topicThreadService, times(0)).createNewTopicThread(any(), any(), any());
    }

    @Test
    void processCreateThread_noSuchForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.getForumByName(anyString())).thenReturn(testTopicForum);
        when(topicThreadService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);
        when(messageService.getMessage(eq("Exception.forum.notfound"))).thenReturn("Forum was not found.");

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
        when(topicThreadService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

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
        when(topicThreadService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

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
        when(topicThreadService.createNewTopicThread(any(), any(), any())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/forum/" + testTopicForum.getName() + "/processCreateThread")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", testTopicForumThread.getTitle())
                .param("firstPostContent", testTopicForumThread.getPosts().first().getContent()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("create-thread-page"))
                .andExpect(model().hasErrors());
    }

    @Test
    void showThread_validThread_notLoggedIn() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("topic-thread-page"))
                .andExpect(model().attributeExists("forumName"))
                .andExpect(model().attributeExists("threadId"))
                .andExpect(model().attributeExists("threadTitle"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeDoesNotExist("postCreationDto", "loggedInUser", "voteMap",
                        "canLock", "canUnlock"));
    }

    @Test
    void showThread_validThread_loggedIn() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("topic-thread-page"))
                .andExpect(model().attributeExists("forumName"))
                .andExpect(model().attributeExists("threadId"))
                .andExpect(model().attributeExists("threadTitle"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("postCreationDto", "loggedInUser", "voteMap",
                        "canLock", "canUnlock"))
                .andExpect(model().attribute("canLock", false))
                .andExpect(model().attribute("canUnlock", false));
    }

    @Test
    void showThread_validThread_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testUser);
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));
    }

    @Test
    void showThread_noSuchForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(false);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(testTopicForumThread);
        when(messageService.getMessage(eq("Exception.forum.notfound"))).thenReturn("Forum was not found.");

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andExpect(view().name("forum-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void showThread_noSuchThreadOnGivenForum() throws Exception {
        when(forumService.isForumWithNameExists(anyString())).thenReturn(true);
        when(topicThreadService.getThreadByForumNameAndId(anyString(), anyLong())).thenReturn(null);
        when(messageService.getMessage("Exception.thread.notfound")).thenReturn("Thread was not found.");

        String url = "/forum/" + testTopicForumThread.getForum().getName() + "/show/" + testTopicForumThread.getId();
        mockMvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andExpect(view().name("thread-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void processLockThread_valid() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);
        when(topicThreadService.lockThread(any(), any())).thenReturn(true);

        mockMvc.perform(post("/lockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?threadLocked"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(1)).lockThread(any(), any());
    }

    @Test
    void processLockThread_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testAdmin)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/lockTopicThread/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testAdmin.getUsername()));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).lockThread(any(), any());
    }

    @Test
    void processLockThread_lockUnsuccessful() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);
        when(topicThreadService.lockThread(any(), any())).thenReturn(false);

        mockMvc.perform(post("/lockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?lockThreadError"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(1)).lockThread(any(), any());
    }

    @Test
    void processLockThread_nullUser() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(null);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/lockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?lockThreadError"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).lockThread(any(), any());
    }

    @Test
    void processLockThread_nullThread() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(null);
        when(messageService.getMessage(eq("Exception.thread.notfound"))).thenReturn("Thread was not found.");

        mockMvc.perform(post("/lockTopicThread/1"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("thread-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(userService, times(0)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).lockThread(any(), any());
    }

    @Test
    void processLockThread_threadAlreadyLocked() throws Exception {
        testTopicForumThread.lock(testModerator);

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/lockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?threadLocked"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).lockThread(any(), any());
    }

    @Test
    void processUnlockThread_valid() throws Exception {
        testTopicForumThread.lock(testModerator);

        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);
        when(topicThreadService.unlockThread(any(), any())).thenReturn(true);

        mockMvc.perform(post("/unlockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?threadUnlocked"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(1)).unlockThread(any(), any());
    }

    @Test
    void processUnlockThread_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testModerator, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testModerator.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testModerator)).when(disciplineService).handleDisciplinedUser(any());

        testTopicForumThread.lock(testModerator);

        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/unlockTopicThread/1"))
//                .andExpect(status().is3xxRedirection())
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testModerator.getUsername()));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).unlockThread(any(), any());
    }

    @Test
    void processUnlockThread_alreadyUnlocked() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/unlockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?threadUnlocked"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).unlockThread(any(), any());
    }

    @Test
    void processUnlockThread_nullUser() throws Exception {
        testTopicForumThread.lock(testModerator);

        when(userService.getLoggedInUser()).thenReturn(null);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);

        mockMvc.perform(post("/unlockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?unlockThreadError"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(0)).unlockThread(any(), any());
    }

    @Test
    void processUnlockThread_nullThread() throws Exception {
        testTopicForumThread.lock(testModerator);

        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(null);
        when(messageService.getMessage(eq("Exception.thread.notfound"))).thenReturn("Thread was not found.");

        mockMvc.perform(post("/unlockTopicThread/1"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("thread-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(userService, times(0)).getLoggedInUser();
        verify(topicThreadService, times(0)).unlockThread(any(), any());
    }

    @Test
    void processUnlockThread_unlockUnsuccessful() throws Exception {
        testTopicForumThread.lock(testModerator);

        when(userService.getLoggedInUser()).thenReturn(testModerator);
        when(topicThreadService.getThreadById(anyLong())).thenReturn(testTopicForumThread);
        when(topicThreadService.unlockThread(any(), any())).thenReturn(false);

        mockMvc.perform(post("/unlockTopicThread/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/forum/" + testTopicForum.getName() + "/show/1?unlockThreadError"));

        verify(userService, times(1)).getLoggedInUser();
        verify(topicThreadService, times(1)).getThreadById(anyLong());
        verify(topicThreadService, times(1)).unlockThread(any(), any());
    }

}