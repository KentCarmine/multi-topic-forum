package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicThread;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository the provides database access to TopicThreads.
 */
@Repository
public interface TopicThreadRepository extends PagingAndSortingRepository<TopicThread, Long>, SearchTopicThreadRepository {

    List<TopicThread> findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase(String title, String forumName);
}
