package com.kentcarmine.multitopicforum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DisciplineTest {
    private static final int SEC_PER_HOUR = 60 * 60;

    Discipline ban;
    Discipline suspension;

    Date suspendedAt;
    int suspendedAtHoursInPast;
    int suspensionHours;

    User testUser;
    User testUser2;
    User testModerator;

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

        suspendedAtHoursInPast = 48;
        suspensionHours = 72;
        suspendedAt = Date.from(Instant.now().minusSeconds(suspendedAtHoursInPast * SEC_PER_HOUR));
        suspension = new Discipline(testUser, testModerator, DisciplineType.SUSPENSION,
                suspendedAt, suspensionHours,
                "suspension for testing");

        ban = new Discipline(testUser2, testModerator, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)),
                "ban for testing");
    }

    @Test
    void getDisciplineEndTime_suspension() throws Exception {
        Date result = suspension.getDisciplineEndTime();

        Date expectedResult = Date.from(suspendedAt.toInstant().plusSeconds(SEC_PER_HOUR * suspensionHours));
        assertEquals(expectedResult, result);
    }

    @Test
    void getDisciplineEndTime_ban() throws Exception {
        Date result = ban.getDisciplineEndTime();

        assertNull(result);
    }

    @Test
    void isOver_expired() throws Exception {
        suspension.setDisciplineDurationHours(1);

        boolean result = suspension.isOver();

        assertTrue(result);
    }

    @Test
    void isOver_rescinded() throws Exception {
        ban.setRescinded(true);

        boolean result = ban.isOver();

        assertTrue(result);
    }

    @Test
    void isOver_notOver() throws Exception {
        boolean result = suspension.isOver();

        assertFalse(result);
    }
}