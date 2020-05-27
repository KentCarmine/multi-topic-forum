package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Repository the provides database access to forum Posts.
 */
@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Long> {

    Page<Post> findAllByThread(TopicThread thread, Pageable pageable);
}
