package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.bootstrap.Bootstrap;
import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.services.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIT {
    private static final String EXISTING_USER_NAME = "moderator2";
    private static final String EXISTING_USER_NAME_2 = "admin2";


    private MockMvc mockMvc;

    @Autowired
    UserController controller;

    @Autowired
    Bootstrap bootstrap;

    @Mock
    MessageService messageService;

    @BeforeEach
    void setUp() throws Exception {
        bootstrap.run();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();
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
