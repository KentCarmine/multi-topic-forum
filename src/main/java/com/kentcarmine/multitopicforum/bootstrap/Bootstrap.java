package com.kentcarmine.multitopicforum.bootstrap;

import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Adds data to database for testing.
 */
@Component
@Profile({"dev", "test"})
public class Bootstrap implements CommandLineRunner {

    private UserRepository userRepository;
    private TopicForumRepository topicForumRepository;
    private TopicThreadRepository topicThreadRepository;
    private PostRepository postRepository;
    private PostVoteRepository postVoteRepository;
    private DisciplineRepository disciplineRepository;

    @Autowired
    public Bootstrap(UserRepository userRepository, TopicForumRepository topicForumRepository,
                     TopicThreadRepository topicThreadRepository, PostRepository postRepository,
                     PostVoteRepository postVoteRepository, DisciplineRepository disciplineRepository) {
        this.userRepository = userRepository;
        this.topicForumRepository = topicForumRepository;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
        this.disciplineRepository = disciplineRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createUsers();
        createTopicForums();
    }

    private void createTopicForums() {
        TopicForum testForum1 = new TopicForum("Test_Forum_1", "First forum for testing.");
        topicForumRepository.save(testForum1);

        TopicForum testForum2 = new TopicForum("DemoForum2", "Second forum for testing! This is a very " +
                "long description. Second forum for testing! This is a very long description. Second forum for testing! " +
                "This is a very long description. Second forum for testing! This is a very long description. Second " +
                "forum for testing! This is a very long description. Second forum for testing! This is a very long " +
                "description. Second forum for testing! This is a very long description.");
        topicForumRepository.save(testForum2);

        TopicForum testForum3 = new TopicForum("zzz_TestForum3", "Another empty forum for testing");
        topicForumRepository.save(testForum3);

        TopicThread forum2Thread1 = new TopicThread("Thread1 this is a very long thread name, this is a very long thread name, this is a very long thread name, this is a very long thread name", testForum2);
        topicThreadRepository.save(forum2Thread1);

        TopicThread forum2Thread2 = new TopicThread("Thread 2", testForum2);
        topicThreadRepository.save(forum2Thread2);

        final long secondsIn8Hours = (60 * 60 * 8);
        Post post1 = new Post("Test content 1", Date.from(Instant.now().minusSeconds(secondsIn8Hours)));
        post1.setUser(userRepository.findByUsername("admin"));
        post1.setThread(forum2Thread1);
        post1 = postRepository.save(post1);

        Post post2 = new Post("Test content 2", Date.from(Instant.now().minusSeconds(10)));
        post2.setUser(userRepository.findByUsername("user"));
        post2.setThread(forum2Thread1);
        post2 = postRepository.save(post2);

        final long secondsIn12Mins = (60 * 12);
        Post post5 = new Post("Test content 2", Date.from(Instant.now().minusSeconds(secondsIn12Mins)));
        post5.setUser(userRepository.findByUsername("admin2"));
        post5.setThread(forum2Thread2);
        post5 = postRepository.save(post5);

        TopicThread forum1Thread1 = new TopicThread("Thread2", testForum1);
        topicThreadRepository.save(forum1Thread1);

        final long secondsIn3Years20Days = (60 * 60 * 24 * 365 * 3) + (60 * 60 * 24 * 20);
        Post post3 = new Post("Test content 3", Date.from(Instant.now().minusSeconds(secondsIn3Years20Days)));
        post3.setUser(userRepository.findByUsername("user2"));
        post3.setThread(forum1Thread1);
        post3 = postRepository.save(post3);

        final long secondsIn70Days = (60 * 60 * 24 * 70);
        Post post4 = new Post("Test content 4", Date.from(Instant.now().minusSeconds(secondsIn70Days)));
        post4.setUser(userRepository.findByUsername("admin2"));
        post4.setThread(forum1Thread1);
        post4 = postRepository.save(post4);

        final long secondsIn16Days = (60 * 60 * 24 * 16);
        Post post6 = new Post("Test content 6", Date.from(Instant.now().minusSeconds(secondsIn16Days)));
        post6.setUser(userRepository.findByUsername("moderator2"));
        post6.setThread(forum1Thread1);
        post6 = postRepository.save(post6);

        User admin = userRepository.findByUsername("admin");
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut " +
                "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi " +
                "ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
                "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
                "qui officia deserunt mollit anim id est laborum.";
        for (int i = 0; i < 98; i++) {
            Post post = new Post("Dynamic test content " + i + ". " + lorem, java.util.Date.from(Instant.now().minusSeconds(120 - i)));
            post.setUser(admin);
            post.setThread(forum2Thread1);
            postRepository.save(post);
        }

        PostVote vote1 = new PostVote(PostVoteState.UPVOTE, userRepository.findByUsername("admin"), post4);
        vote1 = postVoteRepository.save(vote1);

        PostVote vote2 = new PostVote(PostVoteState.DOWNVOTE, userRepository.findByUsername("admin2"), post1);
        vote2 = postVoteRepository.save(vote2);

        PostVote vote3 = new PostVote(PostVoteState.DOWNVOTE, userRepository.findByUsername("user"), post1);
        vote3 = postVoteRepository.save(vote3);

        PostVote vote4 = new PostVote(PostVoteState.UPVOTE, userRepository.findByUsername("moderator2"), post1);
        vote4 = postVoteRepository.save(vote4);

        forum2Thread1.lock(userRepository.findByUsername("superadmin"));
        topicThreadRepository.save(forum2Thread1);

//        post1 = postRepository.findById(post1.getId()).get();
//        System.out.println("### " + postVoteRepository.findByUserAndPost(userRepository.findByUsername("admin2"), post1));
//        System.out.println("### " + post1.getPostVotes().get(0).toString());\
    }

    private void createUsers() {
        UserRole[] superAdminRoles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR};
        User superAdmin = new User("superadmin", "$2a$10$cttbmjQK2y1T/cUziaBKfuzcQ.6d2.F2jmZTuXcxuMZ.ofdKhy8iC", "superadmin@test.com");
        superAdmin.setEnabled(true);
        superAdmin.addAuthorities(superAdminRoles);
        userRepository.save(superAdmin);

        UserRole[] adminRoles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR};
        User admin1 = new User("admin", "$2a$10$OzkZPlR1KVnET7vNDXRSP.ww5vxB2y2134x26bM3ii0uu6oNrGsca", "admin@test.com");
        admin1.setEnabled(true);
        admin1.addAuthorities(adminRoles);
        userRepository.save(admin1);

        User admin2 = new User("admin2", "$2a$10$5wi4HHICDfbufi4aWBIW/.il6s8X0v8pzPbn2Hn.kpeMJU1EUgLra", "admin2@test.com");
        admin2.setEnabled(true);
        admin2.addAuthorities(adminRoles);
        userRepository.save(admin2);

        UserRole[] modRoles = {UserRole.USER, UserRole.MODERATOR};
        User mod1 = new User("moderator", "$2a$10$hJwauYHCeQnFJMk8kGfo/.zzNuH.TnhNyTCUY.mRXHFkAgGwtsJZm", "moderator@test.com");
        mod1.setEnabled(true);
        mod1.addAuthorities(modRoles);
        userRepository.save(mod1);

        User mod2 = new User("moderator2", "$2a$10$K39s1FmJXgNG1fLY9Njptu3VvE1bxECkT3IdACffDiUerHuB9LSoi", "moderator2@test.com");
        mod2.setEnabled(true);
        mod2.addAuthorities(modRoles);
        userRepository.save(mod2);

        User user1 = new User("user", "$2a$10$VMT4W4hEJ7xhUKXGnLboheJQYzvOQDR41KfPGQu3d4daR8TI0X3je", "user@test.com");
        user1.setEnabled(true);
        user1.addAuthority(UserRole.USER);
        userRepository.save(user1);

        User user2 = new User("user2", "$2a$10$7pPZby2Uu6uITXSv0bMmuOVmb2CDYVr5lewlNSvlZShjU.e4FdClm", "user2@test.com");
        user2.setEnabled(true);
        user2.addAuthority(UserRole.USER);
        userRepository.save(user2);

        User bannedUser = new User("bannedUser", "$2a$10$iG4pEz6PqsUZdseb7ogQFO4.9MBeOEOi5pupms8AIT1DYO6DXJidK", "bannedUser@test.com");
        bannedUser.setEnabled(true);
        bannedUser.addAuthority(UserRole.USER);
        bannedUser = userRepository.save(bannedUser);
        Discipline bannedUserDiscipline = new Discipline(bannedUser, admin1, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(60)), "Ban for testing");
        bannedUser.addDiscipline(bannedUserDiscipline);
        bannedUserDiscipline = disciplineRepository.save(bannedUserDiscipline);
        bannedUser = userRepository.save(bannedUser);

        Discipline inactiveBannedUserDiscipline1 = new Discipline(bannedUser, admin2, DisciplineType.BAN, Date.from(Instant.now().minusSeconds(72000)), "rescinded ban for testing");
        inactiveBannedUserDiscipline1.setRescinded(true);
        bannedUser.addDiscipline(inactiveBannedUserDiscipline1);
        inactiveBannedUserDiscipline1 = disciplineRepository.save(inactiveBannedUserDiscipline1);

        Discipline inactiveBannedUserDiscipline2 = new Discipline(bannedUser, mod2, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(259200)), 48, "expired suspension for testing");
        bannedUser.addDiscipline(inactiveBannedUserDiscipline2);
        inactiveBannedUserDiscipline2 = disciplineRepository.save(inactiveBannedUserDiscipline2);

        bannedUser = userRepository.save(bannedUser);

        User suspendedAdmin = new User("suspendedAdmin", "$2a$10$bWQBSibD0D2Vp4b65kE/guKzq8nHHWJQodvIji/9lETe25cG/9VNe", "suspendedAdmin@test.com");
        suspendedAdmin.setEnabled(true);
        suspendedAdmin.addAuthorities(UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR);
        suspendedAdmin = userRepository.save(suspendedAdmin);
        Discipline suspendedAdminDiscipline = new Discipline(suspendedAdmin, admin2, DisciplineType.SUSPENSION, Date.from(Instant.now().minusSeconds(60)), 1,"Suspension for testing");
        suspendedAdmin.addDiscipline(suspendedAdminDiscipline);
        suspendedAdminDiscipline = disciplineRepository.save(suspendedAdminDiscipline);
        suspendedAdmin = userRepository.save(suspendedAdmin);
    }
}
