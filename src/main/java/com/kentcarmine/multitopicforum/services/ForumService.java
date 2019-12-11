package com.kentcarmine.multitopicforum.services;


import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;

/**
 * Specification for services that provide actions related to Forums
 */
public interface ForumService {

    TopicForum getForumByName(String name);

    boolean isForumWithNameExists(String name);

    TopicForum createForumByDto(TopicForumDto topicForumDto)  throws DuplicateForumNameException;

    TopicForum createForum(TopicForum topicForum) throws DuplicateForumNameException;

    TopicThread createNewTopicThread(TopicThreadCreationDto topicThreadCreationDto, User creatingUser, TopicForum owningForum);

    TopicThread getThreadByForumNameAndId(String forumName, Long id);
}
