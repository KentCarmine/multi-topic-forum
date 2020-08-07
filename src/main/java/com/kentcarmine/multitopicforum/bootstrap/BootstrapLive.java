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
                         MessageService messageService) {
        this.userRepository = userRepository;
        this.topicForumRepository = topicForumRepository;
        this.forumRequestForumName = messageService.getMessage("com.kentcarmine.multitopicforum.requestTopicForumCreationForum.name");
        this.forumRequestForumDescription = messageService.getMessage("com.kentcarmine.multitopicforum.requestTopicForumCreationForum.description");
    }

    @Override
    public void run(String... args) throws Exception {
        setUpSuperadmin();
        setUpTopicForumRequestForum();
    }

    private void setUpSuperadmin() {
        if (userRepository.findByUsername(superadminUsername) == null) {
            UserRole[] superAdminRoles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR};
            User superAdmin = new User(superadminUsername, superadminBcryptPasswordHash, superadminEmail);
            superAdmin.setEnabled(true);
            superAdmin.addAuthorities(superAdminRoles);
            userRepository.save(superAdmin);
        }
    }

    private void setUpTopicForumRequestForum() {
        if (topicForumRepository.findByName(forumRequestForumName) == null) {
            TopicForum forumRequestForum = new TopicForum(forumRequestForumName, forumRequestForumDescription);
            forumRequestForum = topicForumRepository.save(forumRequestForum);
        }
    }

}
