package com.kentcarmine.multitopicforum.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {

    @Test
    void getNextAuthority_exists() throws Exception {
        UserRole result = UserRole.getNextAuthority(UserRole.MODERATOR);

        assertEquals(UserRole.ADMINISTRATOR, result);
    }

    @Test
    void getNextAuthority_doesNotExist() throws Exception {
        UserRole result = UserRole.getNextAuthority(UserRole.SUPER_ADMINISTRATOR);

        assertNull(result);
    }

    @Test
    void getPreviousAuthority_exists() throws Exception {
        UserRole result = UserRole.getPreviousAuthority(UserRole.SUPER_ADMINISTRATOR);

        assertEquals(UserRole.ADMINISTRATOR, result);
    }

    @Test
    void getPreviousAuthority_doesNotExist() throws Exception {
        UserRole result = UserRole.getPreviousAuthority(UserRole.USER);

        assertNull(result);
    }
}