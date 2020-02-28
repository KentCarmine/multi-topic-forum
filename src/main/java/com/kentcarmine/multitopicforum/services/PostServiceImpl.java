package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.PostVoteRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private final TopicForumRepository topicForumRepository;
    private final TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;
    private final TopicThreadRepository topicThreadRepository;
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;

    @Autowired
    public PostServiceImpl(TopicForumRepository topicForumRepository,
                            TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter,
                            TopicThreadRepository topicThreadRepository, PostRepository postRepository,
                            PostVoteRepository postVoteRepository) {
        this.topicForumRepository = topicForumRepository;
        this.topicForumDtoToTopicForumConverter = topicForumDtoToTopicForumConverter;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
    }

    // TODO: Refactor into PostService
    /**
     * Create and save a new Post with the content within the given PostCreationDto, and belonging to the given User
     * and TopicThread.
     * @param postCreationDto the DTO containing the post content
     * @param creatingUser the user creating the post
     * @param thread the thread the post should belong to
     * @return the post
     */
    @Transactional
    @Override
    public Post addNewPostToThread(PostCreationDto postCreationDto, User creatingUser, TopicThread thread) {
        Post post = new Post(postCreationDto.getContent(), getCurrentDate());
        post.setUser(creatingUser);
        post.setThread(thread);
        return postRepository.save(post);
    }

    // TODO: Refactor into PostService
    @Override
    public Post getPostById(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()) {
            return null;
        } else {
            return postOpt.get();
        }
    }

    // TODO: Refactor into PostService
    /**
     * Flag the given post as deleted by the deleting user at the current time.
     *
     * @param post the post to flag as deleted
     * @param deletingUser the user deleting the post
     * @return the updated post after saving
     */
    @Transactional
    @Override
    public Post deletePost(Post post, User deletingUser) {
        if (!post.isDeleted()) {
            post.setDeleted(true);
            post.setDeletedBy(deletingUser);
            post.setDeletedAt(java.sql.Date.from(Instant.now()));
            return postRepository.save(post);
        } else {
            return post;
        }
    }

    // TODO: Refactor into PostService
    /**
     * Restore the given post from deletion.
     *
     * @param post the post to restore
     * @return the restored post after saving
     */
    @Transactional
    @Override
    public Post restorePost(Post post) {
        post.setDeleted(false);
        post.setDeletedBy(null);
        post.setDeletedAt(null);
        return postRepository.save(post);
    }

    // TODO: Refactor into PostService
    /**
     * Get the show URL of a deleted post
     *
     * @param postToDelete the post to display the url for
     * @return the show URL of the deleted post
     */
    @Override
    public String getGetDeletedPostUrl(Post postToDelete) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/forum/" + postToDelete.getThread().getForum().getName()
                + "/show/" + postToDelete.getThread().getId()
                + "#post_id_" + postToDelete.getId();
    }

    // TODO: Refactor into PostService
    /**
     * Get the show URL of a restored post
     *
     * @param postToRestore the post to display the url for
     * @return the show URL of the restored post
     */
    @Override
    public String getRestoredPostUrl(Post postToRestore) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/forum/" + postToRestore.getThread().getForum().getName()
                + "/show/" + postToRestore.getThread().getId()
                + "#post_id_" + postToRestore.getId();
    }

    /**
     * Helper method that gets the current timestamp as a Date.
     *
     * @return the current timestamp
     */
    private Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        return new Date(calendar.getTime().getTime());
    }
}
