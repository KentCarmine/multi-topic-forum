package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.handlers.CustomResponseEntityExceptionHandler;
import com.kentcarmine.multitopicforum.services.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
class AdministrationControllerTest {

    MockMvc mockMvc;

    AdministrationController controller;

//    @Mock
//    MessageSource messageSource;
    @Mock
    MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        controller = new AdministrationController();

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new CustomResponseEntityExceptionHandler(messageService)).build();
    }

    @Test
    void showAdministrationPage() throws Exception {
        mockMvc.perform(get("/administration"))
                .andExpect(status().isOk())
                .andExpect(view().name("administration-home-page"));
    }
}