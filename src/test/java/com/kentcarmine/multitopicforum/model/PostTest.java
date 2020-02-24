package com.kentcarmine.multitopicforum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {
    private static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Volutpat odio facilisis mauris sit amet massa vitae. Cursus " +
            "in hac habitasse platea dictumst quisque sagittis purus. Vel elit scelerisque mauris pellentesque pulvinar " +
            "pellentesque habitant morbi tristique. A diam sollicitudin tempor id eu. Ultrices gravida dictum fusce ut " +
            "placerat. At erat pellentesque adipiscing commodo elit. Ultrices gravida dictum fusce ut placerat orci " +
            "nulla pellentesque. Malesuada proin libero nunc consequat. Diam maecenas sed enim ut sem viverra. Id " +
            "cursus metus aliquam eleifend mi in nulla posuere. Tellus elementum sagittis vitae et leo duis. Senectus " +
            "et netus et malesuada fames ac turpis.";

    Post testPost;

    PostVote upvote1;
    PostVote upvote2;
    PostVote downvote1;

    User testUser;
    User testModerator;
    User testModerator2;
    User testAdmin;
    User testAdmin2;
    User testSuperAdmin;

    @BeforeEach
    void setUp() {
        testUser = new User("TestUser", "password", "testuser@fakeemail.com");
        testUser.addAuthority(UserRole.USER);
        testUser.setEnabled(true);

        testModerator = new User("TestModerator", "password", "testmod@fakeemail.com");
        testModerator.addAuthorities(UserRole.USER, UserRole.MODERATOR);
        testModerator.setEnabled(true);

        testModerator2 = new User("TestModerator2", "password", "testmod2@fakeemail.com");
        testModerator2.addAuthorities(UserRole.USER, UserRole.MODERATOR);
        testModerator2.setEnabled(true);

        testAdmin = new User("TestAdmin", "password", "testadmin@fakeemail.com");
        testAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        testAdmin.setEnabled(true);

        testAdmin2 = new User("TestAdmin2", "password", "testadmin2@fakeemail.com");
        testAdmin2.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        testAdmin2.setEnabled(true);

        testSuperAdmin = new User("TestSuperAdmin", "password", "testsuperadmin@fakeemail.com");
        testSuperAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR);
        testSuperAdmin.setEnabled(true);

        testPost = new Post(LOREM, Date.from(Instant.now().minusSeconds(60)));
        testPost.setId(1l);
        testPost.setUser(testModerator);

        upvote1 = new PostVote(PostVoteState.UPVOTE, testUser, testPost);
        upvote1.setId(3L);
        testPost.addPostVote(upvote1);

        upvote2 = new PostVote(PostVoteState.UPVOTE, testModerator, testPost);
        upvote2.setId(5L);
        testPost.addPostVote(upvote2);

        downvote1 = new PostVote(PostVoteState.DOWNVOTE, testModerator2, testPost);
        downvote1.setId(7L);
        testPost.addPostVote(downvote1);
    }

    @Test
    void getVoteCount_votesExist() throws Exception {
        int result = testPost.getVoteCount();

        assertEquals(1, result);
    }

    @Test
    void getVoteCount_noVotes() throws Exception {
        Post testPost2 = new Post(LOREM, Date.from(Instant.now().minusSeconds(30)));
        testPost2.setId(15l);
        testPost2.setUser(testUser);

        int result = testPost2.getVoteCount();

        assertEquals(0, result);
    }

    @Test
    void getAbbreviatedContent_longContent() throws Exception {
        String result = testPost.getAbbreviatedContent(50);

        assertTrue(result.endsWith("..."));
        assertEquals(53, result.length());
    }

    @Test
    void getAbbreviatedContent_shortContent() throws Exception {
        testPost.setContent("abcdef");
        String result = testPost.getAbbreviatedContent(50);

        assertFalse(result.endsWith("..."));
        assertEquals(testPost.getContent().length(), result.length());
    }

    @Test
    void isDeletableBy_valid() throws Exception {
        boolean result = testPost.isDeletableBy(testAdmin);

        assertTrue(result);
    }

    @Test
    void isDeletableBy_insufficientAuthority() throws Exception {
        boolean result = testPost.isDeletableBy(testModerator2);

        assertFalse(result);
    }

    @Test
    void isRestorableBy_valid() throws Exception {
        testPost.setDeleted(true);
        testPost.setDeletedAt(Date.from(Instant.now()));
        testPost.setDeletedBy(testAdmin);

        boolean result = testPost.isRestorableBy(testSuperAdmin);

        assertTrue(result);
    }

    @Test
    void isRestorableBy_insufficientAuthority() throws Exception {
        testPost.setDeleted(true);
        testPost.setDeletedAt(Date.from(Instant.now()));
        testPost.setDeletedBy(testAdmin);

        boolean result = testPost.isRestorableBy(testAdmin2);

        assertFalse(result);
    }

    @Test
    void isRestorableBy_notDeleted() throws Exception {
        boolean result = testPost.isRestorableBy(testSuperAdmin);

        assertTrue(result);
    }

}