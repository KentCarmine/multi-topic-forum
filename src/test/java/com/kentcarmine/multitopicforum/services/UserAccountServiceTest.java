package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UserAccountServiceTest {

    private static final long DAY = 60 * 60 * 24;
    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_USER_PASSWORD = "testPassword";
    private static final String TEST_USER_EMAIL = "testuser@test.com";

    private static final String TEST_USERNAME_2 = "User2";
    private static final String TEST_USER_2_PASSWORD = TEST_USER_PASSWORD;
    private static final String TEST_USER_2_EMAIL = "user2fortesting@testemail.com";

    private static final String TEST_MOD_USERNAME = "TestModerator";
    private static final String TEST_MOD_PASSWORD = TEST_USER_PASSWORD;
    private static final String TEST_MOD_EMAIL = "modfortesting@testemail.com";

    private static final String TEST_ADMIN_USERNAME = "TestAdmin";
    private static final String TEST_ADMIN_PASSWORD = TEST_USER_PASSWORD;
    private static final String TEST_ADMIN_EMAIL = "adminfortesting@testemail.com";

    private static final String TEST_SUPER_ADMIN_USERNAME = "TestSuperAdmin";
    private static final String TEST_SUPER_ADMIN_PASSWORD = TEST_USER_PASSWORD;
    private static final String TEST_SUPER_ADMIN_EMAIL = "superadminfortesting@testemail.com";

    UserAccountService userAccountService;

    @Mock
    UserService userService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    UserRepository userRepository;

    @Mock
    VerificationTokenRepository verificationTokenRepository;

    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    AuthorityRepository authorityRepository;

    @Mock
    DisciplineRepository disciplineRepository;

    @Mock
    MessageService messageService;

    @Mock
    PasswordEncoder passwordEncoder;

    private UserDtoToUserConverter userDtoToUserConverter;
    private DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter;
    private UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;

    private User testUser;
    private User testUser2;
    private User testModerator;
    private User testAdmin;
    private User testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        userDtoToUserConverter = new UserDtoToUserConverter();
        disciplineToDisciplineViewDtoConverter = new DisciplineToDisciplineViewDtoConverter();
        userToUserRankAdjustmentDtoConverter = new UserToUserRankAdjustmentDtoConverter();

        userAccountService = new UserAccountServiceImpl(userRepository, userDtoToUserConverter, passwordEncoder,
                verificationTokenRepository, passwordResetTokenRepository, authorityRepository, messageService, userService);

        testUser = new User(TEST_USERNAME, TEST_USER_PASSWORD, TEST_USER_EMAIL);
        testUser.addAuthority(UserRole.USER);

        testUser2 = new User(TEST_USERNAME_2, TEST_USER_2_PASSWORD, TEST_USER_2_EMAIL);
        testUser2.addAuthority(UserRole.USER);

        testModerator = new User(TEST_MOD_USERNAME, TEST_MOD_PASSWORD, TEST_MOD_EMAIL);
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        testAdmin = new User(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD, TEST_ADMIN_EMAIL);
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);

        testSuperAdmin = new User(TEST_SUPER_ADMIN_USERNAME, TEST_SUPER_ADMIN_PASSWORD, TEST_SUPER_ADMIN_EMAIL);
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);

        when(passwordEncoder.encode(anyString())).thenReturn(TEST_USER_PASSWORD);
    }

    @Test
    void createUserByUserDto_validUserDto() throws Exception {
        when(userRepository.save(any())).thenReturn(testUser);

        UserDto userDto = new UserDto();
        userDto.setUsername(testUser.getUsername());
        userDto.setPassword(testUser.getPassword());
        userDto.setEmail(testUser.getEmail());

        User result = userAccountService.createUserByUserDto(userDto);
        assertEquals(testUser, result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_validUser() throws Exception {
        when(userRepository.save(any())).thenReturn(testUser);
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        userAccountService.createUser(testUser);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_duplicateEmail() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userAccountService.createUser(testUser));

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void createUser_duplicateUsername() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);
        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.emailExists(anyString())).thenReturn(false);

        assertThrows(DuplicateUsernameException.class, () -> userAccountService.createUser(testUser));

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void createUser_userNameTooShort() throws Exception {
        testUser.setUsername("1");
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        User result = userAccountService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_userNameNull() throws Exception {
        testUser.setUsername(null);
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        User result = userAccountService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_passwordTooShort() throws Exception {
        testUser.setPassword("1");
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        User result = userAccountService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_passwordNull() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        testUser.setPassword(null);

        User result = userAccountService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_invalidEmail() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        testUser.setEmail("broken@fakeEmail");

        User result = userAccountService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_emailNull() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        testUser.setEmail(null);

        User result = userAccountService.createUser(testUser);

        assertNull(result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createVerificationToken() throws Exception {
        userAccountService.createVerificationToken(testUser, "123");

        verify(verificationTokenRepository, times(1)).save(any());
    }

    @Test
    void validatePasswordResetToken_validToken() throws Exception {
        testUser.setEnabled(true);
        PasswordResetToken prToken = new PasswordResetToken();
        prToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        prToken.setToken("123");
        prToken.setUser(testUser);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(prToken);
        boolean result = userAccountService.validatePasswordResetToken(testUser, prToken.getToken());
        assertTrue(result);
        assertTrue(testUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
    }

    @Test
    void validatePasswordResetToken_noSuchUser() throws Exception {
        testUser.setEnabled(true);
        PasswordResetToken prToken = new PasswordResetToken();
        prToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        prToken.setToken("123");
        prToken.setUser(testUser);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(prToken);
        boolean result = userAccountService.validatePasswordResetToken(null, prToken.getToken());
        assertFalse(result);
        assertFalse(testUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
    }

    @Test
    void validatePasswordResetToken_disabledUser() throws Exception {
        testUser.setEnabled(false);
        PasswordResetToken prToken = new PasswordResetToken();
        prToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        prToken.setToken("123");
        prToken.setUser(testUser);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(prToken);
        boolean result = userAccountService.validatePasswordResetToken(testUser, prToken.getToken());
        assertFalse(result);
        assertFalse(testUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
    }

    @Test
    void validatePasswordResetToken_noSuchToken() throws Exception {
        testUser.setEnabled(true);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(null);
        boolean result = userAccountService.validatePasswordResetToken(testUser, null);
        assertFalse(result);
        assertFalse(testUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
    }

    @Test
    void validatePasswordResetToken_tokenDoesNotMatchUser() throws Exception {
        testUser.setEnabled(true);
        PasswordResetToken prToken = new PasswordResetToken();
        prToken.setExpiryDate(Date.from(Instant.now().plusSeconds(DAY)));
        prToken.setToken("123");
        prToken.setUser(testUser);

        User otherUser = new User();
        otherUser.setEnabled(true);
        otherUser.setUsername("otherUser");
        otherUser.setEmail("otherUser@fakeemail.edu");

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(prToken);
        boolean result = userAccountService.validatePasswordResetToken(otherUser, prToken.getToken());
        assertFalse(result);
        assertFalse(testUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
        assertFalse(otherUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
    }

    @Test
    void validatePasswordResetToken_expiredToken() throws Exception {
        testUser.setEnabled(true);
        PasswordResetToken prToken = new PasswordResetToken();
        prToken.setExpiryDate(Date.from(Instant.now().minusSeconds(DAY)));
        prToken.setToken("123");
        prToken.setUser(testUser);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(prToken);
        boolean result = userAccountService.validatePasswordResetToken(testUser, prToken.getToken());
        assertFalse(result);
        assertFalse(testUser.hasAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE));
    }
}