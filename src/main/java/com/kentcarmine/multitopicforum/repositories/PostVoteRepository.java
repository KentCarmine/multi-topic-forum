package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.PostVote;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PostVoteRepository extends CrudRepository<PostVote, Long> {

    PostVote findByUserAndPost(User user, Post post);

    List<PostVote> findAllByUser(User user);

    List<PostVote> findAllByPost(Post post);
}
