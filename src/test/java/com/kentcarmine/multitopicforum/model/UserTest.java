package com.kentcarmine.multitopicforum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    User testUser;
    User testUser2;
    User testModerator;
    User testModerator2;
    User testSuperAdmin;

    @BeforeEach
    void setUp() {
        testUser = new User("TestUser", "password", "testuser@fakeemail.com");
        testUser.addAuthority(UserRole.USER);
        testUser.setEnabled(true);

        testUser2 = new User("TestUser2", "password", "testuser2@fakeemail.com");
        testUser2.addAuthority(UserRole.USER);
        testUser2.setEnabled(true);

        testModerator = new User("TestModerator", "password", "testmod@fakeemail.com");
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);
        testModerator.setEnabled(true);

        testModerator2 = new User("TestModerator2", "password", "testmod2@fakeemail.com");
        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);
        testModerator2.setEnabled(true);

        testSuperAdmin = new User("TestSuperAdmin", "password", "testsuperadmin@fakeemail.com");
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);
        testSuperAdmin.setEnabled(true);

    }

    @Test
    void getGreatestDurationActiveDiscipline_withSuspensionsAndBan() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setId(3L);
        Discipline longestSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        longestSusp.setId(5L);
        Discipline shortSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(24 * secPerHour)), 28, "for testing");
        shortSusp.setId(7L);

        testUser.addDiscipline(longestSusp);
        testUser.addDiscipline(shortSusp);
        testUser.addDiscipline(ban);

        Discipline result =  testUser.getGreatestDurationActiveDiscipline();

        assertEquals(ban.getId(), result.getId());
    }

    @Test
    void getGreatestDurationActiveDiscipline_withOnlySuspensions() throws Exception {
        int secPerHour = 60 * 60;

        Discipline longestDisc = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        longestDisc.setId(5L);
        Discipline shortDisc = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(24 * secPerHour)), 28, "for testing");
        shortDisc.setId(7L);

        testUser.addDiscipline(longestDisc);
        testUser.addDiscipline(shortDisc);

        Discipline result = testUser.getGreatestDurationActiveDiscipline();

        assertEquals(longestDisc.getId(), result.getId());
    }

    @Test
    void getGreatestDurationActiveDiscipline_noDisciplines() throws Exception {
        Discipline result = testUser.getGreatestDurationActiveDiscipline();

        assertNull(result);
    }

    @Test
    void getGreatestDurationActiveDiscipline_noActiveDisciplines() throws Exception {
        Discipline pastDisc = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.EPOCH), 2, "for testing");
        testUser.addDiscipline(pastDisc);

        Discipline result = testUser.getGreatestDurationActiveDiscipline();

        assertNull(result);
    }

    @Test
    void isHigherAuthority_higherAuthority() throws Exception {
        boolean result = testSuperAdmin.isHigherAuthority(testUser2);

        assertTrue(result);
    }

    @Test
    void isHigherAuthority_equalAuthority() throws Exception {
        boolean result = testUser.isHigherAuthority(testUser2);

        assertFalse(result);
    }

    @Test
    void removeAuthority() throws Exception {
        testSuperAdmin.removeAuthority(UserRole.SUPER_ADMINISTRATOR);

        UserRole result = testSuperAdmin.getHighestAuthority();

        assertEquals(3, testSuperAdmin.getAuthorities().size());
        assertEquals(UserRole.ADMINISTRATOR, result);
    }

    @Test
    void hasAuthority() throws Exception {
        assertTrue(testSuperAdmin.hasAuthority(UserRole.ADMINISTRATOR));
        assertFalse(testUser.hasAuthority(UserRole.MODERATOR));
    }

    @Test
    void getIncrementedRank_exists() throws Exception {
        assertEquals(UserRole.MODERATOR, testUser.getIncrementedRank());
    }

    @Test
    void getIncrementedRank_doesNotExist() throws Exception {
        assertNull(testSuperAdmin.getIncrementedRank());
    }

    @Test
    void getDecrementedRank_exists() throws Exception {
        assertEquals(UserRole.ADMINISTRATOR, testSuperAdmin.getDecrementedRank());
    }

    @Test
    void getDecrementedRank_doesNotExist() throws Exception {
        assertNull(testUser.getDecrementedRank());
    }

    @Test
    void isPromotableBy_valid() throws Exception {
        boolean result = testUser.isPromotableBy(testSuperAdmin);

        assertTrue(result);
    }

    @Test
    void isPromotableBy_insufficientRank() throws Exception {
        boolean result = testUser.isPromotableBy(testUser2);

        assertFalse(result);
    }

    @Test
    void isPromotableBy_alreadyMaxRank() throws Exception {
        boolean result = testSuperAdmin.isPromotableBy(testSuperAdmin);

        assertFalse(result);
    }

    @Test
    void isDemotableBy_valid() throws Exception {
        boolean result = testModerator.isDemotableBy(testSuperAdmin);

        assertTrue(result);
    }

    @Test
    void isDemotableBy_insufficientAuthority() throws Exception {
        boolean result = testModerator.isDemotableBy(testModerator2);

        assertFalse(result);
    }

    @Test
    void isDemotableBy_alreadyMinRank() throws Exception {
        boolean result = testUser.isDemotableBy(testModerator);

        assertFalse(result);
    }

    @Test
    void isBannedOrSuspended_true() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setRescinded(true);
        ban.setId(3L);
        Discipline susp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        susp.setId(5L);

        testUser.addDiscipline(susp);
        testUser.addDiscipline(ban);

        boolean result = testUser.isBannedOrSuspended();

        assertTrue(result);
    }

    @Test
    void isBannedOrSuspended_false() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setRescinded(true);
        ban.setId(3L);
        Discipline susp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        susp.setRescinded(true);
        susp.setId(5L);

        testUser.addDiscipline(susp);
        testUser.addDiscipline(ban);

        boolean result = testUser.isBannedOrSuspended();

        assertFalse(result);
    }

    @Test
    void isBanned_true() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setId(3L);
        Discipline susp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        susp.setId(5L);

        testUser.addDiscipline(susp);
        testUser.addDiscipline(ban);

        boolean result = testUser.isBanned();

        assertTrue(result);
    }

    @Test
    void isBanned_false() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setRescinded(true);
        ban.setId(3L);
        Discipline susp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        susp.setId(5L);

        testUser.addDiscipline(susp);
        testUser.addDiscipline(ban);

        boolean result = testUser.isBanned();

        assertFalse(result);
    }

    @Test
    void getActiveDisciplines_present() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setId(3L);
        Discipline longestSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        longestSusp.setId(5L);
        Discipline shortSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(24 * secPerHour)), 28, "for testing");
        shortSusp.setId(7L);

        testUser.addDiscipline(ban);
        testUser.addDiscipline(longestSusp);
        testUser.addDiscipline(shortSusp);

        Set<Discipline> results = testUser.getActiveDisciplines();

        assertEquals(3, results.size());
        assertTrue(results.contains(ban));
        assertTrue(results.contains(longestSusp));
        assertTrue(results.contains(shortSusp));
    }

    @Test
    void getActiveDisciplines_none() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setRescinded(true);
        ban.setId(3L);
        Discipline longestSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 24, "for testing");
        longestSusp.setId(5L);

        testUser.addDiscipline(ban);
        testUser.addDiscipline(longestSusp);

        Set<Discipline> results = testUser.getActiveDisciplines();

        assertEquals(0, results.size());
    }

    @Test
    void getInactiveDisciplines_timedOutAndRescinded() throws Exception {
        int secPerHour = 60 * 60;

        Discipline ban = new Discipline(testUser, testSuperAdmin, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(90 * secPerHour)), "for testing");
        ban.setRescinded(true);
        ban.setId(3L);
        Discipline longestSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(48 * secPerHour)), 72, "for testing");
        longestSusp.setId(5L);
        Discipline shortSusp = new Discipline(testUser, testSuperAdmin, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(24 * secPerHour)), 16, "for testing");
        shortSusp.setId(7L);

        testUser.addDiscipline(ban);
        testUser.addDiscipline(longestSusp);
        testUser.addDiscipline(shortSusp);

        Set<Discipline> results = testUser.getInactiveDisciplines();

        assertEquals(2, results.size());
        assertTrue(results.contains(shortSusp));
        assertTrue(results.contains(ban));
    }

    @Test
    void getInactiveDisciplines_none() throws Exception {
        Set<Discipline> results = testUser.getInactiveDisciplines();

        assertEquals(0, results.size());
    }
}