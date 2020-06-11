package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchTopicThreadRepository {

    Page<TopicThread> searchForTopicThreadsInForum(String forumName, String searchText, Pageable pageable);

}
