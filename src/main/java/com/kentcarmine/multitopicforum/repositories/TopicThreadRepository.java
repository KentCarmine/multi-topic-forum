package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicThread;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository the provides database access to TopicThreads.
 */
@Repository
public interface TopicThreadRepository extends CrudRepository<TopicThread, Long> {
}
