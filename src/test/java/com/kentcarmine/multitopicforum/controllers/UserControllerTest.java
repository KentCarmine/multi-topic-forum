package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.dtos.UserEmailDto;
import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.dtos.UserSearchResultDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.*;
import org.assertj.core.data.TemporalUnitOffset;
import org.assertj.core.internal.bytebuddy.matcher.CollectionSizeMatcher;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import javax.mail.internet.MimeMessage;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class UserControllerTest {
    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

    private static final String TEST_USERNAME_2 = "TestUser2";
    private static final String TEST_USER_2_PASSWORD = "testPassword2";
    private static final String TEST_USER_2_EMAIL = "testuser2@test.com";

    private static final String TEST_ADMIN_USERNAME = "TestAdmin";
    private static final String TEST_ADMIN_PASSWORD = "testAdminPassword";
    private static final String TEST_ADMIN_EMAIL = "testadmin@test.com";

    private static final String TEST_SUPER_ADMIN_USERNAME = "TestSuperAdmin";
    private static final String TEST_SUPER_ADMIN_PASSWORD = "testSuperAdminPassword";
    private static final String TEST_SUPER_ADMIN_EMAIL = "testsuperadmin@test.com";

    MockMvc mockMvc;

    UserController userController;

    @Mock
    UserService userService;

    @Mock
    MessageService messageService;

    @Mock
    DisciplineService disciplineService;

    @Mock
    TopicThreadService topicThreadService;

    User testUser;
    User testUser2;
    User testAdmin;
    User testSuperAdmin;

    UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userToUserRankAdjustmentDtoConverter = new UserToUserRankAdjustmentDtoConverter();

        userController = new UserController(userService, disciplineService, topicThreadService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);

        testUser2 = new User(TEST_USERNAME_2, TEST_USER_2_PASSWORD, TEST_USER_2_EMAIL);
        testUser2.addAuthority(UserRole.USER);

        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);

        testSuperAdmin = new User(TEST_SUPER_ADMIN_USERNAME, TEST_SUPER_ADMIN_PASSWORD, TEST_SUPER_ADMIN_EMAIL);
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);
    }

    @Test
    void showUserPage_validUser_noOneLoggedIn() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.getUser(any())).thenReturn(testUser);
        when(topicThreadService.getPostPageByUser(any(), anyInt(), anyInt())).thenReturn(new PageImpl<Post>(new ArrayList<Post>()));

        mockMvc.perform(get("/users/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeDoesNotExist("userRankAdjustmentDto"));
    }

    @Test
    void showUserPage_validUser_someoneLoggedIn() throws Exception {
        UserRankAdjustmentDto userRankAdjustmentDto = userToUserRankAdjustmentDtoConverter.convert(testUser);
        userRankAdjustmentDto.setPromotableByLoggedInUser(testUser.isPromotableBy(testAdmin));
        userRankAdjustmentDto.setDemotableByLoggedInUser(testUser.isPromotableBy(testAdmin));

        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUserRankAdjustmentDtoForUser(any(), any())).thenReturn(userRankAdjustmentDto);
        when(topicThreadService.getPostPageByUser(any(), anyInt(), anyInt())).thenReturn(new PageImpl<Post>(new ArrayList<Post>()));

        mockMvc.perform(get("/users/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userRankAdjustmentDto"));
    }

    @Test
    void showUserPage_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.getUser(any())).thenReturn(testUser);

        mockMvc.perform(get("/users/" + testUser.getUsername()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));
    }

    @Test
    void showUserPage_invalidUser() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("User was not found.");

        mockMvc.perform(get("/users/doesNotExistUsername"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void showUsersListPage_withoutSearch() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-search-page"))
                .andExpect(model().attributeExists("userSearchDto"))
                .andExpect(model().attributeDoesNotExist("usernames"));

//        verify(userService, times(0)).searchForUsers(anyString());
//        verify(userService, times(0)).searchForUsernames(anyString());
        verify(userService, times(0)).searchForUserDtosPaginated(anyString(), anyInt(), anyInt());
//        verify(userService, times(0)).searchForUsernames(anyString());
    }

    @Test
    void showUsersListPage_validSearch() throws Exception {
        String searchText = "user";
        String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);

        List<UserSearchResultDto> userDtoList = new ArrayList<UserSearchResultDto>();
        UserSearchResultDto dto1 = new UserSearchResultDto(testUser.getUsername(), null);
        userDtoList.add(dto1);
        UserSearchResultDto dto2 = new UserSearchResultDto(testUser2.getUsername(), null);
        userDtoList.add(dto2);

        Pageable pageReq = PageRequest.of(0, 25);
        Page<UserSearchResultDto> searchResults = new PageImpl<UserSearchResultDto>(userDtoList,pageReq, userDtoList.size());

        when(userService.searchForUserDtosPaginated(anyString(), anyInt(), anyInt())).thenReturn(searchResults);

        MvcResult res = mockMvc.perform(get("/users?search=" + urlSafeSearchText))
                .andExpect(status().isOk()).andReturn();

        assertEquals("user-search-page",  res.getModelAndView().getViewName());
        assertTrue(res.getModelAndView().getModel().containsKey("userSearchDto"));
        assertTrue(res.getModelAndView().getModel().containsKey("userSearchResults"));
        assertTrue(res.getModelAndView().getModel().get("userSearchResults") instanceof Page);
        Page<UserSearchResultDto> searchResultDtos = (Page<UserSearchResultDto>)res.getModelAndView().getModel().get("userSearchResults");
        assertEquals(0, searchResultDtos.getNumber());
        assertEquals(1, searchResultDtos.getTotalPages());
        assertEquals(2, searchResultDtos.getNumberOfElements());
        assertEquals(2, searchResultDtos.getTotalElements());
        assertEquals(testUser.getUsername(), searchResultDtos.getContent().get(0).getUsername());
        assertEquals(testUser2.getUsername(), searchResultDtos.getContent().get(1).getUsername());

        verify(userService, times(1)).searchForUserDtosPaginated(anyString(), anyInt(), anyInt());
    }

    @Test
    void showUsersListPage_invalidSearch() throws Exception {
        mockMvc.perform(get("/users?searchError"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-search-page"))
                .andExpect(model().attributeExists("userSearchDto"))
                .andExpect(model().attributeDoesNotExist("userSearchResults"));

//        verify(userService, times(0)).searchForUsernames(anyString());
        verify(userService, times(0)).searchForUserDtosPaginated(anyString(), anyInt(), anyInt());
    }

    @Test
    void showUsersListPage_emptySearch() throws Exception {
        String searchText = "";
        String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);

//        SortedSet<String> usernamesResult = new TreeSet<>((o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));

        List<UserSearchResultDto> userDtoList = new ArrayList<UserSearchResultDto>();
        UserSearchResultDto dto1 = new UserSearchResultDto(testUser.getUsername(), null);
        userDtoList.add(dto1);
        UserSearchResultDto dto2 = new UserSearchResultDto(testUser2.getUsername(), null);
        userDtoList.add(dto2);
        UserSearchResultDto dto3 = new UserSearchResultDto(testAdmin.getUsername(), null);
        userDtoList.add(dto3);
        UserSearchResultDto dto4 = new UserSearchResultDto(testSuperAdmin.getUsername(), null);
        userDtoList.add(dto4);

        Pageable pageReq = PageRequest.of(0, 25);
        Page<UserSearchResultDto> expectedPage = new PageImpl<UserSearchResultDto>(userDtoList, pageReq, userDtoList.size());

        when(userService.searchForUserDtosPaginated(anyString(), anyInt(), anyInt())).thenReturn(expectedPage);

        MvcResult res = mockMvc.perform(get("/users?search=" + urlSafeSearchText))
                .andExpect(status().isOk()).andReturn();

        assertEquals("user-search-page",  res.getModelAndView().getViewName());
        assertTrue(res.getModelAndView().getModel().containsKey("userSearchDto"));
        assertTrue(res.getModelAndView().getModel().containsKey("userSearchResults"));
        assertTrue(res.getModelAndView().getModel().get("userSearchResults") instanceof Page);
        Page<UserSearchResultDto> searchResultDtos = (Page<UserSearchResultDto>)res.getModelAndView().getModel().get("userSearchResults");
        assertEquals(0, searchResultDtos.getNumber());
        assertEquals(1, searchResultDtos.getTotalPages());
        assertEquals(4, searchResultDtos.getNumberOfElements());
        assertEquals(4, searchResultDtos.getTotalElements());
        assertEquals(testUser.getUsername(), searchResultDtos.getContent().get(0).getUsername());
        assertEquals(testSuperAdmin.getUsername(), searchResultDtos.getContent().get(3).getUsername());

        verify(userService, times(1)).searchForUserDtosPaginated(anyString(), anyInt(), anyInt());
    }

    @Test
    void processesSearchForUsers_validSearch() throws Exception {
        String searchText = "\"test search\" text";
        String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);

        mockMvc.perform(post("/processSearchUsers")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", searchText))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users?search=" + urlSafeSearchText))
                .andExpect(model().hasNoErrors());
    }

    @Test
    void processesSearchForUsers_invalidSearch() throws Exception {
        mockMvc.perform(post("/processSearchUsers")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("searchText", "invalid\""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users?searchError"))
                .andExpect(model().hasErrors());
    }

}