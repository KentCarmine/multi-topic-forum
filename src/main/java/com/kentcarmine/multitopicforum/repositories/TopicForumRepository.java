package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicForum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository the provides database access to TopicForums.
 */
@Repository
public interface TopicForumRepository extends PagingAndSortingRepository<TopicForum, String>, SearchTopicForumRepository {
    TopicForum findByName(String name);

    List<TopicForum> findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(String searchTerm, String duplicateSearchTerm);

    Page<TopicForum> findAll(Pageable pageable);

    Page<TopicForum> findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase(String searchTerm, String duplicateSearchTerm, Pageable pageable);
}
