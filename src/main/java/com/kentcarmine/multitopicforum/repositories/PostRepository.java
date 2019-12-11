package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository the provides database access to forum Posts.
 */
@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
}
