package com.kentcarmine.multitopicforum.services;


import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;
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

    Map<Long, Integer> generateVoteMap(User loggedInUser, TopicThread thread);

    Post getPostById(Long id);

    PostVote getPostVoteByUserAndPost(User user, Post post);

    PostVoteResponseDto handlePostVoteSubmission(User loggedInUser, Post post, PostVoteSubmissionDto postVoteSubmissionDto);

    void deletePost(Post post, User deletingUser);
}
