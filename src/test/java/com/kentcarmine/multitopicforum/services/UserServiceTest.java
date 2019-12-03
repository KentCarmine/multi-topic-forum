package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import com.kentcarmine.multitopicforum.repositories.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.*;

class UserServiceTest {
    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

    UserService userService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    UserRepository userRepository;

    @Mock
    VerificationTokenRepository verificationTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    private UserDtoToUserConverter userDtoToUserConverter;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        userDtoToUserConverter = new UserDtoToUserConverter();
        userService =
                new UserServiceImpl(userRepository, authenticationService, userDtoToUserConverter, passwordEncoder,
                        verificationTokenRepository);

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);

        when(passwordEncoder.encode(anyString())).thenReturn(TEST_USER_PASSWORD);
    }

    @Test
    void getUser_nullName() {
        assertNull(userService.getUser(null));

        verify(userRepository, times(0)).findByUsername(anyString());
    }

    @Test
    void getUser_existingUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        User result = userService.getUser(testUser.getUsername());
        assertEquals(testUser, result);

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void getUser_nonExistentUser() {
        assertNull(userService.getUser("NoSuchUser"));

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void createUserByUserDto_validUserDto() {
        when(userRepository.save(any())).thenReturn(testUser);

        UserDto userDto = new UserDto();
        userDto.setUsername(testUser.getUsername());
        userDto.setPassword(testUser.getPassword());
        userDto.setEmail(testUser.getEmail());

        User result = userService.createUserByUserDto(userDto);
        assertEquals(testUser, result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_validUser() {
        when(userRepository.save(any())).thenReturn(testUser);

        userService.createUser(testUser);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_duplicateEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(testUser));

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void createUser_duplicateUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        assertThrows(DuplicateUsernameException.class, () -> userService.createUser(testUser));

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void createUser_userNameTooShort() {
        testUser.setUsername("1");

        User result = userService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_userNameNull() {
        testUser.setUsername(null);

        User result = userService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_passwordTooShort() {
        testUser.setPassword("1");

        User result = userService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_passwordNull() {
        testUser.setPassword(null);

        User result = userService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_invalidEmail() {
        testUser.setEmail("broken@fakeEmail");

        User result = userService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_emailNull() {
        testUser.setEmail(null);

        User result = userService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void emailExists_nonExistentEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        assertFalse(userService.emailExists("noSuchEmail@fakeEmail.com"));

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void emailExists_existingEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        assertTrue(userService.emailExists(testUser.getEmail()));

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void usernameExists_nonExistentUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        assertFalse(userService.usernameExists("noSuchUserName"));

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void usernameExists_existingUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        assertTrue(userService.usernameExists(testUser.getUsername()));

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void createVerificationToken() {
        userService.createVerificationToken(testUser, "123");

        verify(verificationTokenRepository, times(1)).save(any());
    }
}