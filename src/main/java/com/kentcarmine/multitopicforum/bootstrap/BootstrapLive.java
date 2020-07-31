package com.kentcarmine.multitopicforum.bootstrap;

import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.*;
import com.kentcarmine.multitopicforum.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod"})
public class BootstrapLive implements Bootstrap {

    private UserRepository userRepository;
    private TopicForumRepository topicForumRepository;
    private TopicThreadRepository topicThreadRepository;
    private PostRepository postRepository;
    private MessageService messageService;

    @Value("${com.kentcarmine.multitopicforum.superadmin.username}")
    private String superadminUsername;

    @Value("${com.kentcarmine.multitopicforum.superadmin.email}")
    private String superadminEmail;

    @Value("${com.kentcarmine.multitopicforum.superadmin.superadminBcryptPasswordHash}")
    private String superadminBcryptPasswordHash;

    private String forumRequestForumName;

    private String forumRequestForumDescription;

    @Autowired
    public BootstrapLive(UserRepository userRepository, TopicForumRepository topicForumRepository,
                        TopicThreadRepository topicThreadRepository, PostRepository postRepository,
                         MessageService messageService) {
        this.userRepository = userRepository;
        this.topicForumRepository = topicForumRepository;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.messageService = messageService;
        this.forumRequestForumName = messageService.getMessage("com.kentcarmine.multitopicforum.requestTopicForumCreationForum.name");
        this.forumRequestForumDescription = messageService.getMessage("com.kentcarmine.multitopicforum.requestTopicForumCreationForum.description");
    }

    @Override
    public void run(String... args) throws Exception {
//        System.out.println("### SuperadminUsername = " + superadminUsername);
//        System.out.println("### SuperadminEmail = " + superadminEmail);
//        System.out.println("### SuperadminBcryptPasswordHash = " + superadminBcryptPasswordHash);
//        System.out.println();
//        System.out.println("### ForumRequestForumDescription = " + forumRequestForumDescription);
//        System.out.println("### ForumRequestForumName = " + forumRequestForumName);
//        System.out.println();

        setUpSuperadmin();
        setUpTopicForumRequestForum();
    }

    private void setUpSuperadmin() {
        UserRole[] superAdminRoles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR};
        User superAdmin = new User(superadminUsername, superadminBcryptPasswordHash, superadminEmail);
        superAdmin.setEnabled(true);
        superAdmin.addAuthorities(superAdminRoles);
        userRepository.save(superAdmin);
    }

    private void setUpTopicForumRequestForum() {
        TopicForum forumRequestForum = new TopicForum(forumRequestForumName, forumRequestForumDescription);
        forumRequestForum = topicForumRepository.save(forumRequestForum);
    }

}
