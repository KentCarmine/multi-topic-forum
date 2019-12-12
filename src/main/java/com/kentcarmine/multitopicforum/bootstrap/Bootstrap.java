package com.kentcarmine.multitopicforum.bootstrap;

import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


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

    @Autowired
    public Bootstrap(UserRepository userRepository, TopicForumRepository topicForumRepository, TopicThreadRepository topicThreadRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.topicForumRepository = topicForumRepository;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        topicForumRepository.deleteAll();
        topicThreadRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        createUsers();
        createTopicForums();
    }

    private void createTopicForums() {
        TopicForum testForum1 = new TopicForum("Test_Forum_1", "First forum for testing.");
        topicForumRepository.save(testForum1);

        TopicForum testForum2 = new TopicForum("TestForum2", "Second forum for testing!");
        topicForumRepository.save(testForum2);

        TopicThread forum2Thread1 = new TopicThread("Thread1", testForum2);
        topicThreadRepository.save(forum2Thread1);

        Post post1 = new Post("Test content 1", Date.from(Instant.now()));
        post1.setUser(userRepository.findByUsername("admin"));
        post1.setThread(forum2Thread1);
        post1 = postRepository.save(post1);

        Post post2 = new Post("Test content 2", Date.from(Instant.now().plusSeconds(10)));
        post2.setUser(userRepository.findByUsername("user"));
        post2.setThread(forum2Thread1);
        post2 = postRepository.save(post2);

        SortedSet<Post> postList = new TreeSet<>();
        postList.add(post1);
        postList.add(post2);
        forum2Thread1.setPosts(postList);
        forum2Thread1 = topicThreadRepository.save(forum2Thread1);
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
    }
}
