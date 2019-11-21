package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.bootstrap.Bootstrap;
import com.kentcarmine.multitopicforum.config.SecurityConfig;
import com.kentcarmine.multitopicforum.helpers.LoginAuthenticationSuccessHandler;
import com.kentcarmine.multitopicforum.services.CustomUserDetailsService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@ExtendWith(SpringExtension.class)
//@EnableAutoConfiguration
//@AutoConfigureTestDatabase
//@Import(SecurityConfig.class)
//@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@SpringBootTest
//@AutoConfigureMockMvc
class UserControllerIT {
    private static final String EXISTING_USER_NAME = "moderator2";
    private static final String EXISTING_USER_EMAIL = "moderator2@test.com";
    private static final String EXISTING_USER_NAME_2 = "admin2";
    private static final String NON_EXISTING_USER_NAME = "fakeUser123";
    private static final String NON_EXISTING_USER_EMAIL = "fakeUser123@fakeTest.com";

//    @Autowired
    private MockMvc mockMvc;

//    @MockBean
////    UserService userService;
////
////    @MockBean
////    DataSource dataSource;
////
////    @MockBean
////    CustomUserDetailsService userDetailsService;
////
////    @MockBean
////    LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler;

    @Autowired
    UserController controller;

    @Autowired
    Bootstrap bootstrap;

    @BeforeEach
    void setUp() throws Exception {
        bootstrap.run();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * When showing login form, show Login form if no user is logged in
     */
    @WithAnonymousUser
    @Test
    void showLoginForm_LoggedOut() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-form"));
    }

    /**
     * When showing login form, if user is logged in, redirect to that user's home page instead of showing login form
     */
    @WithUserDetails(value=EXISTING_USER_NAME)
    @Test
    void showLoginForm_LoggedIn() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + EXISTING_USER_NAME));
    }

    /**
     * When showing user registration form, show registration form if no user is logged in
     */
    @WithAnonymousUser
    @Test
    void showUserRegistrationForm_LoggedOut() throws Exception{
        mockMvc.perform(get("/registerUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-registration-form"));
    }

    /**
     * When showing user registration form, if user is logged in, redirect to that user's home page instead of showing
     * registration form
     */
    @WithUserDetails(EXISTING_USER_NAME)
    @Test
    void showUserRegistrationForm_LoggedIn() throws Exception{
        mockMvc.perform(get("/registerUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + EXISTING_USER_NAME));
    }

    /**
     * When posting to register a user while already logged in, redirect to logged in user's page
     */
    @WithUserDetails(EXISTING_USER_NAME)
    @Test
    void processUserRegistration_asLoggedInUser() throws Exception {
        mockMvc.perform(post("/processUserRegistration")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", "fakeemail@fake.com")
                .param("password", "fakePassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + EXISTING_USER_NAME));
    }

    /**
     * When posting to register a valid user while not logged in, redirect to login
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_newValidUser() throws Exception {
        String password = "newPassword";
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", NON_EXISTING_USER_EMAIL)
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login?registrationSuccess"));
    }

    /**
     * When posting to register a user with a duplicate username while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_duplicateUsername() throws Exception {
        String password = "newPassword";
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", EXISTING_USER_NAME)
                .param("email", NON_EXISTING_USER_EMAIL)
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When posting to register a user with a duplicate email while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_duplicateEmail() throws Exception {
        String password = "newPassword";
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", EXISTING_USER_EMAIL)
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When posting to register a user with a null password while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_nullPassword() throws Exception {
        String password = null;
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", NON_EXISTING_USER_EMAIL)
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When posting to register a user with a too short password while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_tooShortPassword() throws Exception {
        String password = "";
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", NON_EXISTING_USER_EMAIL)
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When posting to register a user with mismatched passwords while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_passwordMismatch() throws Exception {
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", NON_EXISTING_USER_EMAIL)
                .param("password", "password1")
                .param("confirmPassword", "password2"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When posting to register a user with a too short username while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_tooShortUserName() throws Exception {
        String password = "newPassword";
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "")
                .param("email", NON_EXISTING_USER_EMAIL)
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When posting to register a user with an invalid email format while not logged in, redisplay the registration form
     * with errors
     */
    @WithAnonymousUser
    @Test
    void processUserRegistration_invalidEmail() throws Exception {
        String password = "newPassword";
        mockMvc.perform(post("/processUserRegistration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", NON_EXISTING_USER_NAME)
                .param("email", "test@test.")
                .param("password", password)
                .param("confirmPassword", password))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(view().name("user-registration-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * When requesting a user's page as anonymous user, show that page.
     */
    @WithAnonymousUser
    @Test
    void showUserPage_LoggedOutUser() throws Exception {
        mockMvc.perform(get("/users/" + EXISTING_USER_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"));
    }

    /**
     * When requesting a user's page as a logged in user, show that page.
     */
    @WithUserDetails(EXISTING_USER_NAME)
    @Test
    void showUserPage_LoggedInUser() throws Exception {
        mockMvc.perform(get("/users/" + EXISTING_USER_NAME_2))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"));
    }

}