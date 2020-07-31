package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.DisciplineType;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class DisciplineControllerTest {

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

    DisciplineController disciplineController;

    @Mock
    UserService userService;

    @Mock
    MessageService messageService;

    @Mock
    DisciplineService disciplineService;

    User testUser;
    User testUser2;
    User testAdmin;
    User testSuperAdmin;

    UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userToUserRankAdjustmentDtoConverter = new UserToUserRankAdjustmentDtoConverter();

        disciplineController = new DisciplineController(userService, disciplineService);

        mockMvc = MockMvcBuilders.standaloneSetup(disciplineController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

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
    void showManageUserDisciplinePage_validUser() throws Exception {
        List<DisciplineViewDto> discs = new ArrayList<>();
        Pageable pageReq = PageRequest.of(1, 1);
        Page<DisciplineViewDto> inactiveDiscPage = new PageImpl<DisciplineViewDto>(discs, pageReq, discs.size());
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);
        when(disciplineService.getActiveDisciplinesForUser(any(), any())).thenReturn(new TreeSet<DisciplineViewDto>());
        when(disciplineService.getInactiveDisciplineDtosForUserPaginated(any(), anyInt(), anyInt(), any())).thenReturn(inactiveDiscPage);

        mockMvc.perform(get("/manageUserDiscipline/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto", "activeDisciplines",
                        "inactiveDisciplines"));
    }

    @Test
    void showManageUserDisciplinePage_noSuchUser() throws Exception {
        when(userService.getUser(any())).thenReturn(null);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("User was not found.");

        mockMvc.perform(get("/manageUserDiscipline/fakeUserDoesNotExistForTesting"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeDoesNotExist("userDisciplineSubmissionDto"));
    }

    @Test
    void showManageUserDisciplinePage_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testAdmin)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);

        mockMvc.perform(get("/manageUserDiscipline/fakedata"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testAdmin.getUsername()));
    }

    @Test
    void showManageUserDisciplinePage_insufficientAuthority() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(null);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);

        mockMvc.perform(get("/manageUserDiscipline/" + testAdmin.getUsername()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("access-denied-page"))
                .andExpect(model().attributeDoesNotExist("userDisciplineSubmissionDto", "activeDisciplines",
                        "inactiveDisciplines"));
    }

    @Test
    void processUserDisciplineSubmission_valid() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", "ban for testing");

        String expectedUrl = "redirect:/users/" + testUser.getUsername() + "?userDisciplined";

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);
        when(disciplineService.disciplineUser(any(), any())).thenReturn(true);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + userDisciplineSubmissionDto.getDisciplinedUsername() + "?userDisciplined"))
                .andExpect(model().hasNoErrors());

        verify(disciplineService, times(1)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_duplicateBan() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", "ban for testing");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);
        when(disciplineService.disciplineUser(any(), any())).thenReturn(false);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + userDisciplineSubmissionDto.getDisciplinedUsername() + "?userAlreadyBanned"))
                .andExpect(model().hasNoErrors());

        verify(disciplineService, times(1)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testAdmin)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", testAdmin.getUsername())
                .param("disciplineType", "Ban")
                .param("reason", discipline.getReason()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testAdmin.getUsername()));

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_nullUsername() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(null, "Ban", "ban for testing");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"));

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_blankUsername() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto("   ", "Ban", "ban for testing");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"));

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_nullReason() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", null);

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"))
                .andExpect(model().hasErrors());

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_blankReason() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", "   ");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"))
                .andExpect(model().hasErrors());

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_suspensionDurationNotNumeric() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Suspension", "suspension for testing");
        userDisciplineSubmissionDto.setSuspensionHours("-9awrjhg-awrhgn");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("suspensionHours", userDisciplineSubmissionDto.getSuspensionHours())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"))
                .andExpect(model().hasErrors());

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_suspensionDurationOutOfRange() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Suspension", "suspension for testing");
        userDisciplineSubmissionDto.setSuspensionHours("-70");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("suspensionHours", userDisciplineSubmissionDto.getSuspensionHours())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"))
                .andExpect(model().hasErrors());

        verify(disciplineService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void showDisciplineInfoPage_valid() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(disciplineService.getLoggedInUserBannedInformationMessage(any())).thenReturn("permaban");

        mockMvc.perform(get("/showDisciplineInfo/" + testAdmin.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-discipline-info-page"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("username", testAdmin.getUsername()));

        verify(userService, times(1)).forceLogOut(any(), any(), any());
    }

    @Test
    void showDisciplineInfoPage_loggedInUserNull() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(userService.getLoggedInUser()).thenReturn(null);

        mockMvc.perform(get("/showDisciplineInfo/" + testAdmin.getUsername()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andExpect(model().attributeDoesNotExist("message"))
                .andExpect(model().attributeDoesNotExist("username"));

        verify(userService, times(0)).forceLogOut(any(), any(), any());
    }

    @Test
    void showDisciplineInfoPage_pageForOtherUser() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(userService.getLoggedInUser()).thenReturn(testSuperAdmin);

        mockMvc.perform(get("/showDisciplineInfo/" + testAdmin.getUsername()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testSuperAdmin.getUsername()));

        verify(userService, times(0)).forceLogOut(any(), any(), any());
    }

    @Test
    void showDisciplineInfoPage_noActiveDisciplines() throws Exception {
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);

        mockMvc.perform(get("/showDisciplineInfo/" + testAdmin.getUsername()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));

        verify(userService, times(0)).forceLogOut(any(), any(), any());
    }

    @Test
    void processRescindDiscipline_valid() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        discipline.setId(12L);
        testAdmin.addDiscipline(discipline);

        when(userService.getLoggedInUser()).thenReturn(testSuperAdmin);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(disciplineService.getDisciplineByIdAndUser(12L, testAdmin)).thenReturn(discipline);

        mockMvc.perform(post("/rescindDiscipline/" + testAdmin.getUsername() + "/" + discipline.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/manageUserDiscipline/" + testAdmin.getUsername()));

        verify(disciplineService, times(1)).rescindDiscipline(any());
    }

    @Test
    void processRescindDiscipline_bannedUserLoggedIn() throws Exception {
        Discipline disciplineForAdmin = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        disciplineForAdmin.setId(8L);
        testAdmin.addDiscipline(disciplineForAdmin);

        Discipline discipline = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        discipline.setId(12L);
        testUser.addDiscipline(discipline);

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        doThrow(new DisciplinedUserException(testAdmin)).when(disciplineService).handleDisciplinedUser(any());

        mockMvc.perform(post("/rescindDiscipline/" + testUser.getUsername() + "/" + discipline.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testAdmin.getUsername()));

        verify(disciplineService, times(0)).rescindDiscipline(any());
        verify(disciplineService, times(0)).getDisciplineByIdAndUser(anyLong(), any());
    }

    @Test
    void processRescindDiscipline_targetUserNull() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testSuperAdmin);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(null);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("User with the username " + testAdmin.getUsername() + " was not found.");

        mockMvc.perform(post("/rescindDiscipline/" + testAdmin.getUsername() + "/" + 12))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"))
                .andExpect(model().attributeExists("message"));

        verify(disciplineService, times(0)).rescindDiscipline(any());
    }

    @Test
    void processRescindDiscipline_noDisciplineToRescind() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testSuperAdmin);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(disciplineService.getDisciplineByIdAndUser(12L, testAdmin)).thenReturn(null);
        when(messageService.getMessage(anyString())).thenReturn("Discipline was not found");

        mockMvc.perform(post("/rescindDiscipline/" + testAdmin.getUsername() + "/" + 12))
                .andExpect(status().isNotFound())
                .andExpect(view().name("general-error-page"))
                .andExpect(model().attributeExists("message"));

        verify(disciplineService, times(0)).rescindDiscipline(any());
    }

    @Test
    void processRescindDiscipline_insufficientAuthority() throws Exception {
        Discipline discipline = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        discipline.setId(12L);
        testUser.addDiscipline(discipline);

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);
        when(disciplineService.getDisciplineByIdAndUser(12L, testUser)).thenReturn(discipline);

        mockMvc.perform(post("/rescindDiscipline/" + testUser.getUsername() + "/" + discipline.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("access-denied-page"));

        verify(disciplineService, times(0)).rescindDiscipline(any());
    }
}