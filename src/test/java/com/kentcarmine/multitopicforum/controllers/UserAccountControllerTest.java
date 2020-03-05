package com.kentcarmine.multitopicforum.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DemoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.DemoteUserSubmissionDto;
import com.kentcarmine.multitopicforum.dtos.PromoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.PromoteUserSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Date;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class UserAccountControllerTest {
    private static final String ROOT_URL = "localhost:8080";

    private static final long DAY = 60 * 60 * 24;

    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

    private static final String TEST_USERNAME_2 = "TestUser2";
    private static final String TEST_USER_2_PASSWORD = "testPassword2";
    private static final String TEST_USER_2_EMAIL = "testuser2@test.com";

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


    MockMvc mockMvc;

    UserAccountController userAccountController;

    @Mock
    UserService userService;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    MessageService messageService;

    @Mock
    UserAccountService userAccountService;

    @Mock
    EmailService emailService;

    @Mock
    DisciplineService disciplineService;

    User testUser;
    User testUser2;
    User testModerator;
    User testModerator2;
    User testAdmin;
    User testSuperAdmin;

    UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userToUserRankAdjustmentDtoConverter = new UserToUserRankAdjustmentDtoConverter();

        userAccountController = new UserAccountController(userService, applicationEventPublisher, emailService, userAccountService, disciplineService, messageService);

        mockMvc = MockMvcBuilders.standaloneSetup(userAccountController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);

        testUser2 = new User(TEST_USERNAME_2, TEST_USER_2_PASSWORD, TEST_USER_2_EMAIL);
        testUser2.addAuthority(UserRole.USER);

        testModerator = new User(TEST_MODERATOR_USERNAME, TEST_MODERATOR_PASSWORD, TEST_MODERATOR_EMAIL);
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testModerator2 = new User(TEST_MODERATOR_2_USERNAME, TEST_MODERATOR_2_PASSWORD, TEST_MODERATOR_2_EMAIL);
        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);

        testSuperAdmin = new User(TEST_SUPER_ADMIN_USERNAME, TEST_SUPER_ADMIN_PASSWORD, TEST_SUPER_ADMIN_EMAIL);
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);
    }

    @Test
    void showLoginForm_LoggedOut() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(null);

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-form"));
    }

    @Test
    void showLoginForm_LoggedIn() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + testUser.getUsername()));
    }

    @Test
    void showUserRegistrationForm_LoggedOut() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(null);

        mockMvc.perform(get("/registerUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void showUserRegistrationForm_LoggedIn() throws Exception {
        when(userService.getLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(get("/registerUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + testUser.getUsername()));
    }

    @Test
    void showUserRegistrationForm_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(get("/registerUser"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));
    }

    @Test
    void processUserRegistration_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(disciplineService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", testUser.getUsername())
                .param("email", testUser.getEmail())
                .param("password", testUser.getPassword()))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));

        verify(userAccountService, times(0)).createUserByUserDto(any());
    }

    @Test
    void confirmRegistration_invalidToken() throws Exception {
        when(userAccountService.getVerificationToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("registration-confirmation-error"));
    }

    @Test
    void confirmRegistration_expiredToken() throws Exception {
        VerificationToken invalidToken = new VerificationToken("123", testUser);
        invalidToken.setExpiryDate(Date.from(Instant.EPOCH));

        when(userAccountService.getVerificationToken(anyString())).thenReturn(invalidToken);
        when(userAccountService.isVerificationTokenExpired(any())).thenReturn(true);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("registration-confirmation-error"));
    }

    @Test
    void confirmRegistration_validToken() throws Exception {
        final long DAY = 60 * 60 * 24;
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));

        when(userAccountService.getVerificationToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?registrationSuccess"));
    }

    @Test
    void confirmRegistration_userAlreadyRegistered() throws Exception {
        testUser.setEnabled(true);
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));

        when(userAccountService.getVerificationToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void resendRegistrationEmail_validToken() throws Exception {
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        when(userAccountService.generateNewVerificationToken(anyString())).thenReturn(validToken);
        when(userAccountService.getUserByVerificationToken(anyString())).thenReturn(testUser);

        mockMvc.perform(get("/resendRegistrationEmail?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?regEmailSent"));
    }

    @Test
    void resendRegistrationEmail_invalidToken() throws Exception {
        when(userAccountService.generateNewVerificationToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/resendRegistrationEmail?token=123"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("general-error-page"));
    }

    @Test
    void resendRegistrationEmail_userAlreadyEnabled() throws Exception {
        testUser.setEnabled(true);
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        when(userAccountService.generateNewVerificationToken(anyString())).thenReturn(validToken);
        when(userAccountService.getUserByVerificationToken(anyString())).thenReturn(testUser);

        mockMvc.perform(get("/resendRegistrationEmail?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void showResetPasswordStarterForm() throws Exception {
        mockMvc.perform(get("/resetPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password-starter-form"))
                .andExpect(model().attributeExists("user_email"));
    }

    @Test
    void processResetPasswordStarterForm_isMalformedEmail() throws Exception {
        mockMvc.perform(post("/processResetPasswordStarterForm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "fake@fakeemail"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("reset-password-starter-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void processResetPasswordStarterForm_isNonexistentEmail() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        mockMvc.perform(post("/processResetPasswordStarterForm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "fake@fakeemail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        verify(userAccountService, times(0)).createPasswordResetTokenForUser(any());
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void processResetPasswordStarterForm_isDisabledUser() throws Exception {
        testUser.setEnabled(false);
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(userAccountService.createPasswordResetTokenForUser(any())).thenReturn(new PasswordResetToken());

        mockMvc.perform(post("/processResetPasswordStarterForm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", testUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        verify(userAccountService, times(0)).createPasswordResetTokenForUser(any());
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void processResetPasswordStarterForm_validInput() throws Exception {
        testUser.setEnabled(true);
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(userAccountService.createPasswordResetTokenForUser(any())).thenReturn(new PasswordResetToken());

        mockMvc.perform(post("/processResetPasswordStarterForm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", testUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        verify(userAccountService, times(1)).createPasswordResetTokenForUser(any());
    }

    @Test
    void showChangePasswordForm_validInput() throws Exception {
        testUser.setEnabled(true);
        String url = "/changePassword?username=" + testUser.getUsername() + "&token=123";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().attributeExists("userPasswordDto"));
    }

    @Test
    void showChangePasswordForm_noSuchUser() throws Exception {
        testUser.setEnabled(true);
        String url = "/changePassword?username=madeupuser&token=123";

        when(userService.getUser(anyString())).thenReturn(null);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordResetError"))
                .andExpect(model().attributeDoesNotExist("userPasswordDto"));
    }

    @Test
    void showChangePasswordForm_disabledUser() throws Exception {
        testUser.setEnabled(false);
        String url = "/changePassword?username=" + testUser.getUsername() + "&token=123";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordResetError"))
                .andExpect(model().attributeDoesNotExist("userPasswordDto"));
    }

    @Test
    void showChangePasswordForm_invalidToken() throws Exception {
        testUser.setEnabled(true);
        String url = "/changePassword?username=" + testUser.getUsername() + "&token=123";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(false);

        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordResetError"))
                .andExpect(model().attributeDoesNotExist("userPasswordDto"));
    }

    @Test
    void processChangePasswordForm_validInput() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword";

        testUser.addAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE);

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordUpdateSuccess"));

        verify(userAccountService, times(1)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_invalidCharactersInUsername() throws Exception {
        String username = "invalid/user name";
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword2";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().hasErrors());

        verify(userAccountService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_mismatchedPasswords() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword2";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().hasErrors());

        verify(userAccountService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_shortPasswords() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "a";
        String confirmPassword = "a";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().hasErrors());

        verify(userAccountService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_invalidToken() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userAccountService.validatePasswordResetToken(any(), anyString())).thenReturn(false);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordResetError"));

        verify(userAccountService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processPromoteUser_validPromotion() throws Exception {
        User promotedUser = new User(testUser.getUsername(), testUser.getPassword(), testUser.getEmail(),
                testUser.getAuthorities());
        testUser.addAuthority(UserRole.MODERATOR);

        String purDtoMsg = promotedUser.getUsername() + " promoted to "
                + promotedUser.getHighestAuthority().getDisplayRank() + ".";
        String purDtoNewPromoteButtonUrl = ROOT_URL
                + "/promoteUserButton/" + promotedUser.getUsername();
        String purDtoNewDemoteButtonUrl = ROOT_URL
                + "/demoteUserButton/" + promotedUser.getUsername();

        PromoteUserResponseDto purDto = new PromoteUserResponseDto(purDtoMsg, purDtoNewPromoteButtonUrl, purDtoNewDemoteButtonUrl);

        when(userService.getUser(any())).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);
        when(userService.isValidPromotionRequest(any(), any(), any())).thenReturn(true);
        when(userService.promoteUser(any())).thenReturn(promotedUser);
        when(userService.getPromoteUserResponseDtoForUser(any())).thenReturn(purDto);

        PromoteUserSubmissionDto req = new PromoteUserSubmissionDto(testUser.getUsername(), UserRole.MODERATOR.name());

        MvcResult result = mockMvc.perform(post("/promoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals(testUser.getUsername() + " promoted to " + UserRole.MODERATOR.getDisplayRank() + ".", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertTrue(promoteButtonUrl.endsWith("/promoteUserButton/" + testUser.getUsername()));

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertTrue(demoteButtonUrl.endsWith("/demoteUserButton/" + testUser.getUsername()));

        verify(userService, times(1)).promoteUser(any());
    }

    @Test
    void processPromoteUser_invalidPromotion() throws Exception {
        when(userService.getUser(any())).thenReturn(testUser);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.isValidPromotionRequest(any(), any(), any())).thenReturn(false);
        when(messageService.getMessage(eq("Exception.authority.insufficient")))
                .thenReturn("You do not have the authority to perform that action.");

        PromoteUserSubmissionDto req = new PromoteUserSubmissionDto(testUser.getUsername(), UserRole.MODERATOR.name());

        MvcResult result = mockMvc.perform(post("/promoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("You do not have the authority to perform that action.", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertNull(promoteButtonUrl);

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertNull(demoteButtonUrl);

        verify(userService, times(0)).promoteUser(any());
    }

    @Test
    void processPromoteUser_loggedInUserNull() throws Exception {
        when(userService.getUser(any())).thenReturn(testUser);
        when(userService.getLoggedInUser()).thenReturn(null);
        when(messageService.getMessage(eq("Exception.authority.insufficient")))
                .thenReturn("You do not have the authority to perform that action.");

        PromoteUserSubmissionDto req = new PromoteUserSubmissionDto(testUser.getUsername(), UserRole.MODERATOR.name());

        MvcResult result = mockMvc.perform(post("/promoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("You do not have the authority to perform that action.", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertNull(promoteButtonUrl);

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertNull(demoteButtonUrl);

        verify(userService, times(0)).isValidPromotionRequest(any(), any(), any());
        verify(userService, times(0)).promoteUser(any());
    }

    @Test
    void processPromoteUser_promotionTargetUserNull() throws Exception {
        when(userService.getUser(any())).thenReturn(null);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(messageService.getMessage(eq("Exception.user.notfound"), anyString()))
                .thenReturn("User with the username " + testUser.getUsername() + " was not found.");

        PromoteUserSubmissionDto req = new PromoteUserSubmissionDto(testUser.getUsername(), UserRole.MODERATOR.name());

        MvcResult result = mockMvc.perform(post("/promoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isNotFound())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("User with the username " + testUser.getUsername() + " was not found.", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertNull(promoteButtonUrl);

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertNull(demoteButtonUrl);

        verify(userService, times(0)).isValidPromotionRequest(any(), any(), any());
        verify(userService, times(0)).promoteUser(any());
    }

    @Test
    void processDemoteUser_validDemotion() throws Exception {
        User demotedUser = new User(testModerator.getUsername(), testModerator.getPassword(), testModerator.getEmail(), testModerator.getAuthorities());
        demotedUser.removeAuthority(UserRole.MODERATOR);

        String durMsg = testModerator.getUsername() + " demoted to " + demotedUser.getHighestAuthority().getDisplayRank()
                + ".";
        String expectedNewPromoteButtonUrl = ROOT_URL + "/promoteUserButton/" + demotedUser.getUsername();
        String expectedNewDemoteButtonUrl = ROOT_URL + "/demoteUserButton/" + demotedUser.getUsername();

        DemoteUserResponseDto durDto = new DemoteUserResponseDto(durMsg, expectedNewPromoteButtonUrl,
                expectedNewDemoteButtonUrl);

        when(userService.getUser(anyString())).thenReturn(testModerator);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);
        when(userService.isValidDemotionRequest(any(), any(), any())).thenReturn(true);
        when(userService.demoteUser(any())).thenReturn(demotedUser);
        when(userService.getDemoteUserResponseDtoForUser(any())).thenReturn(durDto);

        DemoteUserSubmissionDto req = new DemoteUserSubmissionDto(testModerator.getUsername(), UserRole.USER.name());

        MvcResult result = mockMvc.perform(post("/demoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals(testModerator.getUsername() + " demoted to " + UserRole.USER.getDisplayRank() + ".", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertTrue(promoteButtonUrl.endsWith("/promoteUserButton/" + testModerator.getUsername()));

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertTrue(demoteButtonUrl.endsWith("/demoteUserButton/" + testModerator.getUsername()));

        verify(userService, times(1)).isValidDemotionRequest(any(), any(), any());
        verify(userService, times(1)).demoteUser(any());
    }

    @Test
    void processDemoteUser_invalidDemotion() throws Exception {
        User demotedUser = new User(testModerator.getUsername(), testModerator.getPassword(), testModerator.getEmail(), testModerator.getAuthorities());
        demotedUser.removeAuthority(UserRole.MODERATOR);

        when(userService.getUser(anyString())).thenReturn(testModerator);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);
        when(userService.isValidDemotionRequest(any(), any(), any())).thenReturn(false);
        when(messageService.getMessage(eq("Exception.authority.insufficient")))
                .thenReturn("You do not have the authority to perform that action.");

        DemoteUserSubmissionDto req = new DemoteUserSubmissionDto(testModerator.getUsername(), UserRole.USER.name());

        MvcResult result = mockMvc.perform(post("/demoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("You do not have the authority to perform that action.", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertNull(promoteButtonUrl);

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertNull(demoteButtonUrl);

        verify(userService, times(1)).isValidDemotionRequest(any(), any(), any());
        verify(userService, times(0)).demoteUser(any());
    }

    @Test
    void processDemoteUser_demotingNullUser() throws Exception {
        User demotedUser = new User(testModerator.getUsername(), testModerator.getPassword(), testModerator.getEmail(), testModerator.getAuthorities());
        demotedUser.removeAuthority(UserRole.MODERATOR);

        when(userService.getUser(anyString())).thenReturn(null);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(messageService.getMessage(eq("Exception.user.notfound"), anyString()))
                .thenReturn("User with the username " + testUser.getUsername() + " was not found.");

        DemoteUserSubmissionDto req = new DemoteUserSubmissionDto(testModerator.getUsername(), UserRole.USER.name());

        MvcResult result = mockMvc.perform(post("/demoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isNotFound())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("User with the username " + testUser.getUsername() + " was not found.", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertNull(promoteButtonUrl);

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertNull(demoteButtonUrl);

        verify(userService, times(0)).isValidDemotionRequest(any(), any(), any());
        verify(userService, times(0)).demoteUser(any());
    }

    @Test
    void processDemoteUser_loggedInUserNull() throws Exception {
        User demotedUser = new User(testModerator.getUsername(), testModerator.getPassword(), testModerator.getEmail(), testModerator.getAuthorities());
        demotedUser.removeAuthority(UserRole.MODERATOR);

        when(userService.getUser(anyString())).thenReturn(testModerator);
        when(userService.getLoggedInUser()).thenReturn(null);
        when(messageService.getMessage(eq("Exception.authority.insufficient")))
                .thenReturn("You do not have the authority to perform that action.");

        DemoteUserSubmissionDto req = new DemoteUserSubmissionDto(testModerator.getUsername(), UserRole.USER.name());

        MvcResult result = mockMvc.perform(post("/demoteUserAjax")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(req)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String resStr = result.getResponse().getContentAsString();

        String msg = JsonPath.read(resStr, "$.message");
        assertEquals("You do not have the authority to perform that action.", msg);

        String promoteButtonUrl = JsonPath.read(resStr, "$.newPromoteButtonUrl");
        assertNull(promoteButtonUrl);

        String demoteButtonUrl = JsonPath.read(resStr, "$.newDemoteButtonUrl");
        assertNull(demoteButtonUrl);

        verify(userService, times(0)).isValidDemotionRequest(any(), any(), any());
        verify(userService, times(0)).demoteUser(any());
    }

    @Test
    void demoteUserButton_valid() throws Exception {
        when(userService.getUser(any())).thenReturn(testModerator);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);

        MvcResult mvcResult = mockMvc.perform(get("/demoteUserButton/" + testModerator.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/promote-demote-buttons :: demote-button-fragment"))
                .andReturn();

        ModelAndView mv = mvcResult.getModelAndView();
        assertTrue(mv.getModel().containsKey("userRankAdjustmentDto"));
    }

    @Test
    void demoteUserButton_nullUser() throws Exception {
        when(userService.getUser(any())).thenReturn(null);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);

        MvcResult mvcResult = mockMvc.perform(get("/demoteUserButton/" + testModerator.getUsername()))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ModelAndView mv = mvcResult.getModelAndView();
        assertFalse(mv.getModel().containsKey("userRankAdjustmentDto"));
    }

    @Test
    void demoteUserButton_nullLoggedInUser() throws Exception {
        when(userService.getUser(any())).thenReturn(testModerator);
//        when(userService.getLoggedInUser()).thenReturn(null);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(get("/demoteUserButton/" + testModerator.getUsername()))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ModelAndView mv = mvcResult.getModelAndView();
        assertFalse(mv.getModel().containsKey("userRankAdjustmentDto"));
    }

    @Test
    void promoteUserButton_valid() throws Exception {
        when(userService.getUser(any())).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);

        MvcResult mvcResult = mockMvc.perform(get("/promoteUserButton/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/promote-demote-buttons :: promote-button-fragment"))
                .andReturn();

        ModelAndView mv = mvcResult.getModelAndView();
        assertTrue(mv.getModel().containsKey("userRankAdjustmentDto"));
    }

    @Test
    void promoteUserButton_nullUser() throws Exception {
        when(userService.getUser(any())).thenReturn(null);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(testAdmin);

        MvcResult mvcResult = mockMvc.perform(get("/promoteUserButton/" + TEST_USERNAME))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ModelAndView mv = mvcResult.getModelAndView();
        assertFalse(mv.getModel().containsKey("userRankAdjustmentDto"));
    }

    @Test
    void promoteUserButton_nullLoggedInUser() throws Exception {
        when(userService.getUser(any())).thenReturn(testUser);
        when(userService.getLoggedInUserIfNotDisciplined()).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(get("/promoteUserButton/" + testUser.getUsername()))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ModelAndView mv = mvcResult.getModelAndView();
        assertFalse(mv.getModel().containsKey("userRankAdjustmentDto"));
    }

    /**
     * Helper method to convert objects into JSON strings.
     *
     * @param obj the object to convert
     * @return the JSON string representing obj
     */
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}