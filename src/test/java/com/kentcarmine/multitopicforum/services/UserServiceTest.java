package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.security.RunAs;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UserServiceTest {
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

    UserService userService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthorityRepository authorityRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    MessageService messageService;

    private UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;

    private User testUser;
    private User testUser2;
    private User testModerator;
    private User testAdmin;
    private User testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        userToUserRankAdjustmentDtoConverter = new UserToUserRankAdjustmentDtoConverter();

        userService = new UserServiceImpl(userRepository, authenticationService, authorityRepository,
                userToUserRankAdjustmentDtoConverter, messageService);

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
    void getUser_nullName() throws Exception {
        assertNull(userService.getUser(null));

        verify(userRepository, times(0)).findByUsername(anyString());
    }

    @Test
    void getUser_existingUser() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        User result = userService.getUser(testUser.getUsername());
        assertEquals(testUser, result);

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void getUser_nonExistentUser() throws Exception {
        assertNull(userService.getUser("NoSuchUser"));

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void emailExists_nonExistentEmail() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        assertFalse(userService.emailExists("noSuchEmail@fakeEmail.com"));

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void emailExists_existingEmail() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        assertTrue(userService.emailExists(testUser.getEmail()));

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void usernameExists_nonExistentUsername() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        assertFalse(userService.usernameExists("noSuchUserName"));

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void usernameExists_existingUsername() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        assertTrue(userService.usernameExists(testUser.getUsername()));

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void searchForUsers_multipleResults() throws Exception {
        when(userRepository.findByUsernameLikeIgnoreCase(anyString())).thenReturn(List.of(testUser, testUser2));

        SortedSet<User> results = userService.searchForUsers("user");

        assertEquals(2, results.size());
        assertEquals(testUser, results.first());
        assertEquals(testUser2, results.last());

        verify(userRepository, times(1)).findByUsernameLikeIgnoreCase(anyString());
    }

    @Test
    void searchForUsers_noResults() throws Exception {
        when(userRepository.findByUsernameLikeIgnoreCase(anyString())).thenReturn(List.of());

        SortedSet<User> results = userService.searchForUsers("0-qht0g-24nhg");

        assertEquals(0, results.size());

        verify(userRepository, times(1)).findByUsernameLikeIgnoreCase(anyString());
    }

    @Test
    void searchForUsers_emptySearchText() throws Exception {
        when(userRepository.findByUsernameLikeIgnoreCase(anyString())).thenReturn(List.of());

        SortedSet<User> results = userService.searchForUsers("");

        assertEquals(0, results.size());

        verify(userRepository, times(0)).findByUsernameLikeIgnoreCase(anyString());
    }

    @Test
    void searchForUsernames_multipleResults() throws Exception {
        when(userRepository.findByUsernameLikeIgnoreCase(anyString())).thenReturn(List.of(testUser, testUser2));

        SortedSet<String> results = userService.searchForUsernames("user");

        assertEquals(2, results.size());
        assertEquals(testUser.getUsername(), results.first());
        assertEquals(testUser2.getUsername(), results.last());

        verify(userRepository, times(1)).findByUsernameLikeIgnoreCase(anyString());
    }

    @Test
    void searchForUsernames_noResults() throws Exception {
        when(userRepository.findByUsernameLikeIgnoreCase(anyString())).thenReturn(List.of());

        SortedSet<String> results = userService.searchForUsernames("naofg9-03t");

        assertEquals(0, results.size());

        verify(userRepository, times(1)).findByUsernameLikeIgnoreCase(anyString());
    }

    @Test
    void searchForUsernames_emptySearchText() throws Exception {
        when(userRepository.findByUsernameLikeIgnoreCase(anyString())).thenReturn(List.of());

        SortedSet<String> results = userService.searchForUsernames("");

        assertEquals(0, results.size());

        verify(userRepository, times(0)).findByUsernameLikeIgnoreCase(anyString());
    }

    @Test
    void promoteUser() throws Exception {
        assertEquals(testUser.getHighestAuthority(), UserRole.USER);

        User expectedResult = new User(testUser.getUsername(), testUser.getPassword(), testUser.getEmail());
        expectedResult.addAuthorities(UserRole.USER, UserRole.MODERATOR);

        when(userRepository.save(any())).thenReturn(expectedResult);

        User result = userService.promoteUser(testUser);

        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(result.getHighestAuthority(), UserRole.MODERATOR);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void demoteUser() throws Exception {
        testUser.addAuthority(UserRole.MODERATOR);

        User expectedResult = new User(testUser.getUsername(), testUser.getPassword(), testUser.getEmail());
        expectedResult.addAuthorities(UserRole.USER);

        when(userRepository.save(any())).thenReturn(expectedResult);

        User result = userService.demoteUser(testUser);

        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(result.getHighestAuthority(), UserRole.USER);

        verify(authorityRepository, times(1)).deleteByUserAndAuthority(any(), any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void isValidPromotionRequest_isValid() throws Exception {
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.MODERATOR);

        assertTrue(result);
    }

    @Test
    void isValidPromotionRequest_nullInputs() throws Exception {
        boolean result = userService.isValidPromotionRequest(null, null, null);

        assertFalse(result);
    }

    @Test
    void isValidPromotionRequest_userBeingPromotedIsSuperAdmin() throws Exception {
        testUser.addAuthorities(UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.MODERATOR);

        assertFalse(result);
    }

    @Test
    void isValidPromotionRequest_userBeingPromotedToSuperAdmin() throws Exception {
        testUser.addAuthorities(UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.SUPER_ADMINISTRATOR);

        assertFalse(result);
    }

    @Test
    void isValidPromotionRequest_promotionRankIsSuperAdmin() throws Exception {
        testUser.addAuthorities(UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.SUPER_ADMINISTRATOR);

        assertFalse(result);
    }

    @Test
    void isValidPromotionRequest_promoterSameRankAsUserBeingPromoted() throws Exception {
        boolean result = userService.isValidPromotionRequest(testUser2, testUser, UserRole.MODERATOR);

        assertFalse(result);
    }

    @Test
    void isValidPromotionRequest_promoterSameRankAsPromotionTargetRank() throws Exception {
        testUser.addAuthority(UserRole.MODERATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.ADMINISTRATOR);

        assertFalse(result);
    }

    @Test
    void isValidPromotionRequest_promotionRankMismatch() throws Exception {
        testUser.addAuthority(UserRole.MODERATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.MODERATOR);

        assertFalse(result);
    }

    @Test
    void isValidDemotionRequest_isValid() throws Exception {
        testUser.addAuthority(UserRole.MODERATOR);
        boolean result = userService.isValidDemotionRequest(testAdmin, testUser, UserRole.USER);

        assertTrue(result);
    }

    @Test
    void isValidDemotionRequest_nullInputs() throws Exception {
        boolean result = userService.isValidDemotionRequest(null, null, null);

        assertFalse(result);
    }

    @Test
    void isValidDemotionRequest_userAlreadyLowestRank() throws Exception {
        boolean result = userService.isValidDemotionRequest(testAdmin, testUser, UserRole.CHANGE_PASSWORD_PRIVILEGE);

        assertFalse(result);
    }

    @Test
    void isValidDemotionRequest_demotionRankMismatch() throws Exception {
        testAdmin.addAuthority(UserRole.SUPER_ADMINISTRATOR);
        testUser.addAuthorities(UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.USER);

        assertFalse(result);
    }

    @Test
    void isValidDemotionRequest_demotingUserHasInsufficentAuthority() throws Exception {
        testUser.addAuthorities(UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        boolean result = userService.isValidPromotionRequest(testAdmin, testUser, UserRole.MODERATOR);

        assertFalse(result);
    }

    @Test
    void getUserRankAdjustmentDtoForUser() throws Exception {
        UserRankAdjustmentDto dto = userService.getUserRankAdjustmentDtoForUser(testUser, testAdmin);

        assertEquals(testUser.getUsername(), dto.getUsername());
        assertEquals(UserRole.USER, dto.getHighestAuthority());
        assertEquals(UserRole.MODERATOR, dto.getIncrementedRank());
        assertNull(dto.getDecrementedRank());
        assertTrue(dto.isPromotableByLoggedInUser());
        assertFalse(dto.isDemotableByLoggedInUser());
    }

}