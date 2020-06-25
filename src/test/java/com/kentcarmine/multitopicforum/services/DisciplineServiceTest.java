package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.DisciplineType;
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
import java.util.Optional;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
class DisciplineServiceTest {
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

    DisciplineService disciplineService;

    @Mock
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    DisciplineRepository disciplineRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    MessageService messageService;

    private DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter;

    private User testUser;
    private User testUser2;
    private User testModerator;
    private User testAdmin;
    private User testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        disciplineToDisciplineViewDtoConverter = new DisciplineToDisciplineViewDtoConverter();

        disciplineService = new DisciplineServiceImpl(userRepository, disciplineRepository,
                disciplineToDisciplineViewDtoConverter, userService, messageService);

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
    void disciplineUser_ban() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto = new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", "ban for testing");
        User loggedInUser = testAdmin;

        Discipline discipline = new Discipline(testUser, loggedInUser, DisciplineType.BAN, java.util.Date.from(Instant.now()), userDisciplineSubmissionDto.getReason());
        discipline.setId(5L);

        when(userRepository.findByUsername(eq(userDisciplineSubmissionDto.getDisciplinedUsername()))).thenReturn(testUser);
        when(disciplineRepository.save(any())).thenReturn(discipline);
        when(userRepository.save(eq(testUser))).thenReturn(testUser);
        when(userService.getUser(eq(userDisciplineSubmissionDto.getDisciplinedUsername()))).thenReturn(testUser);

        assertFalse(testUser.isBannedOrSuspended());

        boolean result = disciplineService.disciplineUser(userDisciplineSubmissionDto, loggedInUser);

        assertTrue(result);
        assertTrue(testUser.isBannedOrSuspended());
        assertTrue(testUser.isBanned());
        assertEquals(1, testUser.getActiveDisciplines().size());
        assertEquals(DisciplineType.BAN, testUser.getGreatestDurationActiveDiscipline().getDisciplineType());

        verify(userRepository, times(1)).save(any());
        verify(disciplineRepository, times(1)).save(any());
    }

    @Test
    void disciplineUser_duplicateBan() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto = new UserDisciplineSubmissionDto(testUser.getUsername(), "Ban", "ban for testing");
        User loggedInUser = testAdmin;

        Discipline previousBan = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, java.util.Date.from(Instant.now().minusSeconds(180)), "previous ban");
        previousBan.setId(4L);
        testUser.addDiscipline(previousBan);

        Discipline discipline = new Discipline(testUser, loggedInUser, DisciplineType.BAN, java.util.Date.from(Instant.now()), userDisciplineSubmissionDto.getReason());
        discipline.setId(5L);

        when(userRepository.findByUsername(eq(userDisciplineSubmissionDto.getDisciplinedUsername()))).thenReturn(testUser);
        when(disciplineRepository.save(any())).thenReturn(discipline);
        when(userRepository.save(eq(testUser))).thenReturn(testUser);
        when(userService.getUser(eq(userDisciplineSubmissionDto.getDisciplinedUsername()))).thenReturn(testUser);

        assertTrue(testUser.isBannedOrSuspended());

        boolean result = disciplineService.disciplineUser(userDisciplineSubmissionDto, loggedInUser);

        assertFalse(result);
        assertTrue(testUser.isBannedOrSuspended());
        assertTrue(testUser.isBanned());
        assertEquals(1, testUser.getActiveDisciplines().size());
        assertEquals(DisciplineType.BAN, testUser.getGreatestDurationActiveDiscipline().getDisciplineType());

        verify(userRepository, times(0)).save(any());
        verify(disciplineRepository, times(0)).save(any());
    }

    @Test
    void disciplineUser_suspension() throws Exception {
        UserDisciplineSubmissionDto userDisciplineSubmissionDto = new UserDisciplineSubmissionDto(testUser.getUsername(), "Suspension", "Suspension for testing");
        User loggedInUser = testAdmin;

        Discipline discipline = new Discipline(testUser, loggedInUser, DisciplineType.SUSPENSION, java.util.Date.from(Instant.now()), 72, userDisciplineSubmissionDto.getReason());
        discipline.setId(5L);

        when(userRepository.findByUsername(eq(userDisciplineSubmissionDto.getDisciplinedUsername()))).thenReturn(testUser);
        when(disciplineRepository.save(any())).thenReturn(discipline);
        when(userRepository.save(eq(testUser))).thenReturn(testUser);
        when(userService.getUser(eq(userDisciplineSubmissionDto.getDisciplinedUsername()))).thenReturn(testUser);

        assertFalse(testUser.isBannedOrSuspended());

        boolean result = disciplineService.disciplineUser(userDisciplineSubmissionDto, loggedInUser);

        assertTrue(result);
        assertTrue(testUser.isBannedOrSuspended());
        assertEquals(1, testUser.getActiveDisciplines().size());
        assertEquals(DisciplineType.SUSPENSION, testUser.getGreatestDurationActiveDiscipline().getDisciplineType());
        assertEquals(72, testUser.getGreatestDurationActiveDiscipline().getDisciplineDurationHours());

        verify(userRepository, times(1)).save(any());
        verify(disciplineRepository, times(1)).save(any());
    }

    @Test
    void handleDisciplinedUser_userDisciplined() throws Exception {
        User loggedInUser = testAdmin;
        Discipline discipline = new Discipline(testUser, loggedInUser, DisciplineType.BAN, java.util.Date.from(Instant.now()), "Ban for testing");
        discipline.setId(5L);
        testUser.addDiscipline(discipline);

        assertThrows(DisciplinedUserException.class, () -> {
            disciplineService.handleDisciplinedUser(testUser);
        });
    }

    @Test
    void handleDisciplinedUser_userNotDisciplined() throws Exception {
        assertDoesNotThrow(() -> {
            disciplineService.handleDisciplinedUser(testUser);
        });
    }

    @Test
    void getActiveDisciplinesForUser() throws Exception {
        Discipline disc1 = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        disc1.setId(1L);
        testUser.addDiscipline(disc1);

        Discipline disc2 = new Discipline(testUser, testAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(120)), 3,"active suspension for testing");
        disc2.setId(2L);
        testUser.addDiscipline(disc2);

        Discipline disc3 = new Discipline(testUser, testAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(7200)), 1,"expired suspension for testing");
        disc3.setId(3L);
        testUser.addDiscipline(disc3);

        Discipline disc4 = new Discipline(testUser, testAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(180)), 5,"rescinded suspension for testing");
        disc4.setId(4L);
        disc4.setRescinded(true);
        testUser.addDiscipline(disc4);

        Discipline disc5 = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(180)), "rescinded ban for testing");
        disc5.setId(5L);
        disc5.setRescinded(true);
        testUser.addDiscipline(disc5);

        SortedSet<DisciplineViewDto> activeDiscDtos = disciplineService.getActiveDisciplinesForUser(testUser, testAdmin);

        assertEquals(2, activeDiscDtos.size());
    }

    @Test
    void getInactiveDisciplinesForUser() throws Exception {
        // Active
        Discipline disc1 = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        disc1.setId(1L);
        testUser.addDiscipline(disc1);

        // Active
        Discipline disc2 = new Discipline(testUser, testAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(120)), 3,"active suspension for testing");
        disc2.setId(2L);
        testUser.addDiscipline(disc2);

        // Inactive (expired)
        Discipline disc3 = new Discipline(testUser, testAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(7200)), 1,"expired suspension for testing");
        disc3.setId(3L);
        testUser.addDiscipline(disc3);

        // Inactive (rescinded)
        Discipline disc4 = new Discipline(testUser, testAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(180)), 5,"rescinded suspension for testing");
        disc4.setId(4L);
        disc4.setRescinded(true);
        testUser.addDiscipline(disc4);

        // Inactive (rescinded)
        Discipline disc5 = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(180)), "rescinded ban for testing");
        disc5.setId(5L);
        disc5.setRescinded(true);
        testUser.addDiscipline(disc5);

        SortedSet<DisciplineViewDto> inactiveDiscDtos = disciplineService.getInactiveDisciplinesForUser(testUser);

        assertEquals(3, inactiveDiscDtos.size());
    }

    @Test
    void getInactiveDisciplineDtosForUserPaginated_valid() throws Exception {
        // TODO: Fill in
    }

    @Test
    void getInactiveDisciplineDtosForUserPaginated_noResults() throws Exception {
        // TODO: Fill in
    }

    @Test
    void getInactiveDisciplineDtosForUserPaginated_pageTooLow() throws Exception {
        // TODO: Fill in
    }

    @Test
    void getInactiveDisciplineDtosForUserPaginated_pageTooHigh() throws Exception {
        // TODO: Fill in
    }

    @Test
    void getDisciplineByIdAndUser_valid() throws Exception {
        Discipline disc = new Discipline(testUser, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        disc.setId(1L);
        testUser.addDiscipline(disc);

        when(disciplineRepository.findById(anyLong())).thenReturn(Optional.of(disc));

        Discipline result = disciplineService.getDisciplineByIdAndUser(1L, testUser);

        assertNotNull(result);
        assertEquals(disc, result);

        verify(disciplineRepository, times(1)).findById(anyLong());
    }

    @Test
    void getDisciplineByIdAndUser_invalidDisciplineId() throws Exception {
        when(disciplineRepository.findById(anyLong())).thenReturn(Optional.empty());

        Discipline result = disciplineService.getDisciplineByIdAndUser(1L, testUser);

        assertNull(result);

        verify(disciplineRepository, times(1)).findById(anyLong());
    }

    @Test
    void getDisciplineByIdAndUser_mismatchedDisciplineIdAndUser() throws Exception {
        Discipline disc = new Discipline(testUser2, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        disc.setId(1L);
        testUser2.addDiscipline(disc);

        when(disciplineRepository.findById(anyLong())).thenReturn(Optional.of(disc));

        Discipline result = disciplineService.getDisciplineByIdAndUser(1L, testUser);

        assertNull(result);

        verify(disciplineRepository, times(1)).findById(anyLong());


    }

    @Test
    void rescindDiscipline() throws Exception {
        Discipline disc = new Discipline(testUser2, testAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "ban for testing");
        disc.setId(1L);

        assertFalse(disc.isRescinded());

        disciplineService.rescindDiscipline(disc);

        assertTrue(disc.isRescinded());

        verify(disciplineRepository, times(1)).save(any());
    }

}