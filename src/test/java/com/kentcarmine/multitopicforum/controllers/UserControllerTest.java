package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
class UserControllerTest {

    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

    MockMvc mockMvc;

    UserController userController;

    @Mock
    UserService userService;

    User testUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userController = new UserController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

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
}