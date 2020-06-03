package com.kentcarmine.multitopicforum.services;


import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.TopicForum;
import org.springframework.data.domain.Page;

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

    SortedSet<TopicForum> getAllForums();

    SortedSet<TopicForum> searchTopicForums(String searchText) throws UnsupportedEncodingException;

    TopicForumViewDto getTopicForumViewDtoForTopicForum(TopicForum topicForum);

    SortedSet<TopicForumViewDto> getAllForumsAsViewDtos();

    SortedSet<TopicForumViewDto> searchTopicForumsForViewDtos(String searchText) throws UnsupportedEncodingException;

    Page<TopicForumViewDto> getForumsAsViewDtosPaginated(int pageNum);

    Page<TopicForumViewDto> searchTopicForumsForViewDtosPaginated(String searchText, int page) throws UnsupportedEncodingException;

}
