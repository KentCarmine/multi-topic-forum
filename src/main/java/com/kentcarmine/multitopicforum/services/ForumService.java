package com.kentcarmine.multitopicforum.services;


import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDtoLight;
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

    Page<TopicForumViewDto> getForumsAsViewDtosPaginated(int pageNum, int resultsPerPage);

    Page<TopicForum> searchTopicForumsWithCustomQuery(String searchText, int page, int resultsPerPage);

    Page<TopicForumViewDto> searchTopicForumsForViewDtosWithCustomQuery(String searchText, int page, int resultsPerPage);

    TopicForumViewDtoLight getTopicForumViewDtoLightForTopicForum(TopicForum topicForum);
}
