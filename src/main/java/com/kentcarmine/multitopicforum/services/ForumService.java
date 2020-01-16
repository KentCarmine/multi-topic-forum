package com.kentcarmine.multitopicforum.services;


import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;

import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

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

    Post addNewPostToThread(PostCreationDto postCreationDto, User creatingUser, TopicThread thread);

    SortedSet<TopicForum> getAllForums();

    SortedSet<TopicForum> searchTopicForums(String searchText) throws UnsupportedEncodingException;

    SortedSet<TopicThread> searchTopicThreads(String forumName, String searchText) throws UnsupportedEncodingException;
}
