package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.PostVoteRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class TopicThreadServiceImpl implements TopicThreadService {

    private final TopicForumRepository topicForumRepository;
//    private final TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;
    private final TopicThreadRepository topicThreadRepository;
    private final PostRepository postRepository;
//    private final PostVoteRepository postVoteRepository;
    private final ForumService forumService;


    @Autowired
    public TopicThreadServiceImpl(TopicForumRepository topicForumRepository,
//                            TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter,
                            TopicThreadRepository topicThreadRepository, PostRepository postRepository//,
                            /*PostVoteRepository postVoteRepository*/, ForumService forumService) {
        this.topicForumRepository = topicForumRepository;
//        this.topicForumDtoToTopicForumConverter = topicForumDtoToTopicForumConverter;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.forumService = forumService;
//        this.postVoteRepository = postVoteRepository;
    }

    // TODO: Refactor into TopicThreadService
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

    // TODO: Refactor into TopicThreadService
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
        if (forumService.isForumWithNameExists(forumName) && thread != null && thread.getForum().getName().equals(forumName)) {
            return thread;
        } else {
            return null;
        }
    }

    // TODO: Refactor into TopicThreadService
    /**
     * Searches for all topic threads in a topic forum with the given forumName that have titles that contain all tokens
     * (delimited on double quotes and spaces, but not spaces within double quotes) of the given search text. Empty
     * search text or a search of "" returns all threads in the given forum
     *
     * @param forumName the name of the forum to search
     * @param searchText The text to search for
     * @return the set of TopicThreads (ordered reverse chronologically by creation date of the first post) that match
     * the search terms
     * @throws UnsupportedEncodingException
     */
    @Override
    public SortedSet<TopicThread> searchTopicThreads(String forumName, String searchText)
            throws UnsupportedEncodingException {
        SortedSet<TopicThread> threads = new TreeSet<>(new Comparator<TopicThread>() {
            @Override
            public int compare(TopicThread o1, TopicThread o2) {
                return o2.getFirstPost().getPostedAt().compareTo(o1.getFirstPost().getPostedAt()); // Newest threads first
            }
        });

        if (searchText.equals("") || searchText.equals("\"\"")) {
            TopicForum forum = topicForumRepository.findByName(forumName);
            if (forum != null) {
                threads.addAll(forum.getThreads());
            }

            return threads;
        }

        List<String> searchTerms = parseSearchText(searchText);
        List<List<TopicThread>> searchTermResults = new ArrayList<>();
        for (int i = 0; i < searchTerms.size(); i++) {
            searchTermResults.add(new ArrayList<TopicThread>());
        }

        for(int i = 0; i < searchTerms.size(); i++) {
            String st = searchTerms.get(i);
            searchTermResults.set(i, topicThreadRepository
                    .findByTitleLikeIgnoreCaseAndForumNameIsIgnoreCase("%" + st + "%", forumName));
        }

        if (!searchTermResults.isEmpty()) {
            threads.addAll(searchTermResults.get(0));
            searchTermResults.remove(0);
            for (List<TopicThread> str : searchTermResults) {
                threads.retainAll(str);
            }
        }

        return threads;
    }

    // TODO: Refactor into TopicThreadService
    /**
     * Check if the given user can lock the given thread.
     *
     * @param user the user to check for permission to lock the thread
     * @param thread the thread to check
     * @return true if the user can lock the thread, false otherwise
     */
    @Override
    public boolean canUserLockThread(User user, TopicThread thread) {
        if (user == null || thread == null) {
            return false;
        }

        boolean userOutranksThreadCreator = user.isHigherAuthority(thread.getFirstPost().getUser());
        boolean userHasAdministrativeRights = user.isModerator() || user.isAdmin() || user.isSuperadmin();

        return !thread.isLocked() && userHasAdministrativeRights && userOutranksThreadCreator;
    }

    // TODO: Refactor into TopicThreadService
    /**
     * Check if the given user can unlock the given thread.
     *
     * @param user the user to check for permission to unlock the thread
     * @param thread the thread to check
     * @return true if the user can unlock the thread, false otherwise
     */
    @Override
    public boolean canUserUnlockThread(User user, TopicThread thread) {
        if (user == null || thread == null) {
            return false;
        }

        boolean userHasAdministrativeRights = user.isModerator() || user.isAdmin() || user.isSuperadmin();
        boolean userOutranksThreadLocker = thread.getLockingUser() != null && (user.equals(thread.getLockingUser()) ||user.isHigherAuthority(thread.getLockingUser()));

        return thread.isLocked() && userHasAdministrativeRights && userOutranksThreadLocker;
    }

    // TODO: Refactor into TopicThreadService
    /**
     * If the given user has the permissions to lock the given thread, flags that thread as locked and returns true.
     * Otherwise, does nothing and returns false.
     * @param lockingUser the user attempting to lock the thread
     * @param thread the thread to lock
     * @return true if the user succeeded in locking the thread, false otherwise
     */
    @Override
    public boolean lockThread(User lockingUser, TopicThread thread) {
        if (canUserLockThread(lockingUser, thread)) {
            thread.lock(lockingUser);
            topicThreadRepository.save(thread);
            return true;
        } else {
            return false;
        }
    }

    // TODO: Refactor into TopicThreadService
    /**
     * If the given user has the permissions to unlock the given thread, flags that thread as unlocked and returns true.
     * Otherwise, does nothing and returns false.
     * @param unlockingUser the user attempting to unlock the thread
     * @param thread the thread to unlock
     * @return true if the user succeeded in unlocking the thread, false otherwise
     */
    @Override
    public boolean unlockThread(User unlockingUser, TopicThread thread) {
        if (canUserUnlockThread(unlockingUser, thread)) {
            thread.unlock();
            topicThreadRepository.save(thread);
            return true;
        } else {
            return false;
        }
    }

    // TODO: Refactor into TopicThreadService
    /**
     * Gets a thread with the given ID, or null if no such thread exists.
     * @param id the id of the thread to get
     * @return the thread with the given id, or null if no such thread exists
     */
    public TopicThread getThreadById(Long id) {
        Optional<TopicThread> thOpt = topicThreadRepository.findById(id);

        if (thOpt.isPresent()) {
            return thOpt.get();
        } else {
            return null;
        }
    }

    /**
     * Helper method that parses search text.
     *
     * @param searchText the text to be parsed.
     * @return the list of tokens
     * @throws UnsupportedEncodingException
     */
    private List<String> parseSearchText(String searchText) throws UnsupportedEncodingException {
        return SearchParserHelper.parseSearchText(searchText);
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
