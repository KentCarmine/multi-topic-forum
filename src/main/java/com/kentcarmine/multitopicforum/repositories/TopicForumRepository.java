package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicForum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository the provides database access to TopicForums.
 */
@Repository
public interface TopicForumRepository extends CrudRepository<TopicForum, String> {
    TopicForum findByName(String name);
}
