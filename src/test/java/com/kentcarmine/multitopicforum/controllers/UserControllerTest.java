package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.UserEmailDto;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.services.UserService;
import org.assertj.core.data.TemporalUnitOffset;
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

    MockMvc mockMvc;

    UserController userController;

    @Mock
    UserService userService;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    MessageSource messageSource;

    @Mock
    JavaMailSender mailSender;

    User testUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userController = new UserController(userService, applicationEventPublisher, messageSource, mailSender);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageSource)).build();

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);
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
    void showUserPage_validUser() throws Exception {
        // check model too
        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.getUser(any())).thenReturn(testUser);

        mockMvc.perform(get("/users/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"))
                .andExpect(model().attributeExists("user"));
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
        verify(mailSender, times(0)).send(any(MimeMessage.class));
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
        verify(mailSender, times(0)).send(any(MimeMessage.class));
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











}