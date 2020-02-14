package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.dtos.UserEmailDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.services.EmailService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.assertj.core.data.TemporalUnitOffset;
import org.assertj.core.internal.bytebuddy.matcher.CollectionSizeMatcher;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import javax.mail.internet.MimeMessage;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class UserControllerTest {
    private static final long DAY = 60 * 60 * 24;

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
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    MessageSource messageSource;

    @Mock
    EmailService emailService;

    User testUser;
    User testUser2;
    User testAdmin;
    User testSuperAdmin;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userController = new UserController(userService, applicationEventPublisher, messageSource, emailService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageSource)).build();

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

        doThrow(new DisciplinedUserException(testUser)).when(userService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(get("/registerUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));
    }

    @Test
    void processUserRegistration_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(userService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", testUser.getUsername())
                .param("email", testUser.getEmail())
                .param("password", testUser.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));

        verify(userService, times(0)).createUserByUserDto(any());
    }

    @Test
    void showUserPage_validUser() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.getUser(any())).thenReturn(testUser);

        mockMvc.perform(get("/users/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void showUserPage_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testUser.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testUser)).when(userService).handleDisciplinedUser(any());

        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.getUser(any())).thenReturn(testUser);

        mockMvc.perform(get("/users/" + testUser.getUsername()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testUser.getUsername()));
    }

    @Test
    void showUserPage_invalidUser() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);

        mockMvc.perform(get("/users/doesNotExistUsername"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void confirmRegistration_invalidToken() throws Exception {
        when(userService.getVerificationToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("registration-confirmation-error"));
    }

    @Test
    void confirmRegistration_expiredToken() throws Exception {
        VerificationToken invalidToken = new VerificationToken("123", testUser);
        invalidToken.setExpiryDate(Date.from(Instant.EPOCH));

        when(userService.getVerificationToken(anyString())).thenReturn(invalidToken);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("registration-confirmation-error"));
    }

    @Test
    void confirmRegistration_validToken() throws Exception {
        final long DAY = 60 * 60 * 24;
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));

        when(userService.getVerificationToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?registrationSuccess"));
    }

    @Test
    void confirmRegistration_userAlreadyRegistered() throws Exception {
        testUser.setEnabled(true);
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));

        when(userService.getVerificationToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/registrationConfirm?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void resendRegistrationEmail_validToken() throws Exception {
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        when(userService.generateNewVerificationToken(anyString())).thenReturn(validToken);
        when(userService.getUserByVerificationToken(anyString())).thenReturn(testUser);

        mockMvc.perform(get("/resendRegistrationEmail?token=123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?regEmailSent"));
    }

    @Test
    void resendRegistrationEmail_invalidToken() throws Exception {
        when(userService.generateNewVerificationToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/resendRegistrationEmail?token=123"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("general-error-page"));
    }

    @Test
    void resendRegistrationEmail_userAlreadyEnabled() throws Exception {
        testUser.setEnabled(true);
        VerificationToken validToken = new VerificationToken("123", testUser);
        validToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        when(userService.generateNewVerificationToken(anyString())).thenReturn(validToken);
        when(userService.getUserByVerificationToken(anyString())).thenReturn(testUser);

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

        verify(userService, times(0)).createPasswordResetTokenForUser(any());
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void processResetPasswordStarterForm_isDisabledUser() throws Exception {
        testUser.setEnabled(false);
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(userService.createPasswordResetTokenForUser(any())).thenReturn(new PasswordResetToken());

        mockMvc.perform(post("/processResetPasswordStarterForm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", testUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        verify(userService, times(0)).createPasswordResetTokenForUser(any());
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void processResetPasswordStarterForm_validInput() throws Exception {
        testUser.setEnabled(true);
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(userService.createPasswordResetTokenForUser(any())).thenReturn(new PasswordResetToken());

        mockMvc.perform(post("/processResetPasswordStarterForm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", testUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        verify(userService, times(1)).createPasswordResetTokenForUser(any());
    }

    @Test
    void showChangePasswordForm_validInput() throws Exception {
        testUser.setEnabled(true);
        String url = "/changePassword?username=" + testUser.getUsername() + "&token=123";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

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
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

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
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

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
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(false);

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
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordUpdateSuccess"));

        verify(userService, times(1)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_invalidCharactersInUsername() throws Exception {
        String username = "invalid/user name";
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword2";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().hasErrors());

        verify(userService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_mismatchedPasswords() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword2";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().hasErrors());

        verify(userService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_shortPasswords() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "a";
        String confirmPassword = "a";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("change-password-form"))
                .andExpect(model().hasErrors());

        verify(userService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void processChangePasswordForm_invalidToken() throws Exception {
        String username = testUser.getUsername();
        String token = "123";
        String password = "testPassword";
        String confirmPassword = "testPassword";

        when(userService.getUser(anyString())).thenReturn(testUser);
        when(userService.validatePasswordResetToken(any(), anyString())).thenReturn(false);

        mockMvc.perform(post("/processChangePassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?passwordResetError"));

        verify(userService, times(0)).changeUserPassword(any(), anyString());
    }

    @Test
    void showUsersListPage_withoutSearch() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-search-page"))
                .andExpect(model().attributeExists("userSearchDto"))
                .andExpect(model().attributeDoesNotExist("usernames"));

        verify(userService, times(0)).searchForUsers(anyString());
        verify(userService, times(0)).searchForUsernames(anyString());
    }

    @Test
    void showUsersListPage_validSearch() throws Exception {
        String searchText = "user";
        String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);

        SortedSet<String> usernamesResult = new TreeSet<>((o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));
        usernamesResult.add(testUser.getUsername());
        usernamesResult.add(testUser2.getUsername());

        when(userService.searchForUsernames(anyString())).thenReturn(usernamesResult);

        mockMvc.perform(get("/users?search=" + urlSafeSearchText))
                .andExpect(status().isOk())
                .andExpect(view().name("user-search-page"))
                .andExpect(model().attributeExists("userSearchDto"))
                .andExpect(model().attributeExists("usernames"))
                .andExpect(model().attribute("usernames", IsCollectionWithSize.hasSize(usernamesResult.size())));

        verify(userService, times(1)).searchForUsernames(anyString());
    }

    @Test
    void showUsersListPage_invalidSearch() throws Exception {
        mockMvc.perform(get("/users?searchError"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-search-page"))
                .andExpect(model().attributeExists("userSearchDto"))
                .andExpect(model().attributeDoesNotExist("usernames"));

        verify(userService, times(0)).searchForUsernames(anyString());
    }

    @Test
    void showUsersListPage_emptySearch() throws Exception {
        String searchText = "";
        String urlSafeSearchText = URLEncoderDecoderHelper.encode(searchText);

        SortedSet<String> usernamesResult = new TreeSet<>((o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));

        mockMvc.perform(get("/users?search=" + urlSafeSearchText))
                .andExpect(status().isOk())
                .andExpect(view().name("user-search-page"))
                .andExpect(model().attributeExists("userSearchDto"))
                .andExpect(model().attributeExists("usernames"))
                .andExpect(model().attribute("usernames", IsCollectionWithSize.hasSize(usernamesResult.size())));

        verify(userService, times(1)).searchForUsernames(anyString());
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

    @Test
    void showManageUserDisciplinePage_validUser() throws Exception {
        when(userService.getUser(any())).thenReturn(testUser);

        mockMvc.perform(get("/manageUserDiscipline/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"));
    }

    @Test
    void showManageUserDisciplinePage_noSuchUser() throws Exception {
        when(userService.getUser(any())).thenReturn(null);

        mockMvc.perform(get("/manageUserDiscipline/fakeUserDoesNotExistForTesting"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("user-not-found"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeDoesNotExist("userDisciplineSubmissionDto"));
    }

    @Test
    void processUserDisciplineSubmission_valid() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto =
                new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", "ban for testing");

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testUser.getUsername()))).thenReturn(testUser);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", userDisciplineSubmissionDto.getDisciplinedUsername())
                .param("disciplineType", userDisciplineSubmissionDto.getDisciplineType())
                .param("reason", userDisciplineSubmissionDto.getReason()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + userDisciplineSubmissionDto.getDisciplinedUsername() + "?userDisciplined"))
                .andExpect(model().hasNoErrors());

        verify(userService, times(1)).disciplineUser(any(), any());
    }

    @Test
    void processUserDisciplineSubmission_bannedUserLoggedIn() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        doThrow(new DisciplinedUserException(testAdmin)).when(userService).handleDisciplinedUser(any());

        when(userService.getLoggedInUser()).thenReturn(testAdmin);
        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);

        mockMvc.perform(post("/processCreateUserDiscipline")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("disciplinedUsername", testAdmin.getUsername())
                .param("disciplineType", "Ban")
                .param("reason", discipline.getReason()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/showDisciplineInfo/" + testAdmin.getUsername()));

        verify(userService, times(0)).disciplineUser(any(), any());
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
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"))
                .andExpect(model().hasErrors());

        verify(userService, times(0)).disciplineUser(any(), any());
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
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-discipline-page"))
                .andExpect(model().attributeExists("userDisciplineSubmissionDto"))
                .andExpect(model().hasErrors());

        verify(userService, times(0)).disciplineUser(any(), any());
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

        verify(userService, times(0)).disciplineUser(any(), any());
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

        verify(userService, times(0)).disciplineUser(any(), any());
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

        verify(userService, times(0)).disciplineUser(any(), any());
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

        verify(userService, times(0)).disciplineUser(any(), any());
    }

    @Test
    void showDisciplineInfoPage_valid() throws Exception {
        Discipline discipline = new Discipline(testAdmin, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        testAdmin.addDiscipline(discipline);

        when(userService.getUser(eq(testAdmin.getUsername()))).thenReturn(testAdmin);
        when(userService.getLoggedInUser()).thenReturn(testAdmin);

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
}