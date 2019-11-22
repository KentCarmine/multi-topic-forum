package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.security.RunAs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {

    UserService userService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserDtoToUserConverter userDtoToUserConverter;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        userService =
                new UserServiceImpl(userRepository, authenticationService, userDtoToUserConverter, passwordEncoder);

        when(passwordEncoder.encode(anyString())).thenReturn("password");

    }

    @Test
    void getUser() {
    }

    @Test
    void createUserByUserDto() {
    }

    @Test
    void createUser() {
    }

    @Test
    void emailExists() {
    }

    @Test
    void usernameExists() {
    }
}