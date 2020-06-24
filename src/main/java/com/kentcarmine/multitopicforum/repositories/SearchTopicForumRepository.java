package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicForum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchTopicForumRepository {
    List<TopicForum> searchTopicForums(String searchText);

    Page<TopicForum> searchTopicForumsPaginated(String searchText, Pageable page);
}
