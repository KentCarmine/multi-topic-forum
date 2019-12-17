package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Service that provides actions related to TopicForums.
 */
@Service
public class ForumServiceImpl implements ForumService {

    private final TopicForumRepository topicForumRepository;
    private final TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;
    private final TopicThreadRepository topicThreadRepository;
    private final PostRepository postRepository;

    @Autowired
    public ForumServiceImpl(TopicForumRepository topicForumRepository,
                            TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter,
                            TopicThreadRepository topicThreadRepository, PostRepository postRepository) {
        this.topicForumRepository = topicForumRepository;
        this.topicForumDtoToTopicForumConverter = topicForumDtoToTopicForumConverter;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
    }

    @Override
    public TopicForum getForumByName(String name) {
        return topicForumRepository.findByName(name);
    }

    @Override
    public boolean isForumWithNameExists(String name) {
        return topicForumRepository.findByName(name) != null;
    }

    /**
     * Creates and saves a new TopicForum from a TopicForumDto
     * @param topicForumDto the object to save as a TopicForum
     * @return the given forum
     * @throws DuplicateForumNameException if a forum with the same name already exists
     */
    @Override
    public TopicForum createForumByDto(TopicForumDto topicForumDto) throws DuplicateForumNameException {
        return createForum(topicForumDtoToTopicForumConverter.convert(topicForumDto));
    }

    /**
     * Creates and saves a new TopicForum
     * @param topicForum the object to save
     * @return the given forum
     * @throws DuplicateForumNameException if a forum with the same name already exists
     */
    @Transactional
    @Override
    public TopicForum createForum(TopicForum topicForum) throws DuplicateForumNameException {
       if (isForumWithNameExists(topicForum.getName())) {
           throw new DuplicateForumNameException("A topic forum with the name " + topicForum.getName()
                   + " already exists.");
       }

       return topicForumRepository.save(topicForum);
    }

    /**
     * Creates and saves a new TopicThread including its first post the belongs to the given TopicForum and User. The
     * content and title of the thread will be gotten from the topicThreadCreationDto/
     *
     * @param topicThreadCreationDto the DTO contining the title of the thread and content of the first post
     * @param creatingUser the user creating the thread
     * @param owningForum the forum the thread belongs in
     * @return the created TopicThread
     */
    @Transactional
    @Override
    public TopicThread createNewTopicThread(TopicThreadCreationDto topicThreadCreationDto, User creatingUser, TopicForum owningForum) {
        TopicThread topicThread = new TopicThread(topicThreadCreationDto.getTitle(), owningForum);
        topicThread = topicThreadRepository.save(topicThread);

        Post post = new Post(topicThreadCreationDto.getFirstPostContent(), getCurrentDate());
        post.setThread(topicThread);
        post.setUser(creatingUser);
        post = postRepository.save(post);

        return topicThread;
    }

    /**
     * Gets a given TopicThread that has an ID of theadID and that belongs to the forum with the name forumName. If no
     * such thread exists, returns null.
     * @param forumName the forum the thread must belong to
     * @param threadId the id of the thread
     * @return the thread, or null if no such thread exists
     */
    @Override
    public TopicThread getThreadByForumNameAndId(String forumName, Long threadId) {
        TopicThread thread = getThreadById(threadId);
        if (isForumWithNameExists(forumName) && thread != null && thread.getForum().getName().equals(forumName)) {
            return thread;
        } else {
            return null;
        }
    }

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

    /**
     * Helper method that gets a thread with the given ID, or null if no such thread exists.
     * @param id the id of the thread to get
     * @return the thread with the given id, or null if no such thread exists
     */
    private TopicThread getThreadById(Long id) {
        Optional<TopicThread> thOpt = topicThreadRepository.findById(id);

        if (thOpt.isPresent()) {
            return thOpt.get();
        } else {
            return null;
        }
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
