package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository the provides database access to forum Posts.
 */
@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Long> {

    Page<Post> findAllByThread(TopicThread thread, Pageable pageable);

    Page<Post> findAllByUser(User user, Pageable pageable);
}
