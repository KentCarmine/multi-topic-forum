package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;

import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

public interface TopicThreadService {
    TopicThread createNewTopicThread(TopicThreadCreationDto topicThreadCreationDto, User creatingUser, TopicForum owningForum);

    TopicThread getThreadByForumNameAndId(String forumName, Long threadId);

    SortedSet<TopicThread> searchTopicThreads(String forumName, String searchText) throws UnsupportedEncodingException;

    boolean canUserLockThread(User user, TopicThread thread);

    boolean canUserUnlockThread(User user, TopicThread thread);

    boolean lockThread(User lockingUser, TopicThread thread);

    boolean unlockThread(User unlockingUser, TopicThread thread);

    TopicThread getThreadById(Long id);


}