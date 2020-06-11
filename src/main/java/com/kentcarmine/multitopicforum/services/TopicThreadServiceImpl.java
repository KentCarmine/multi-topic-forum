package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadCreationDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicThreadServiceImpl implements TopicThreadService {

    @Value("${spring.data.web.pageable.default-page-size}")
    private int POSTS_PER_PAGE;

    private final TopicForumRepository topicForumRepository;
    private final TopicThreadRepository topicThreadRepository;
    private final PostRepository postRepository;
    private final ForumService forumService;
    private final ForumHierarchyConverter forumHierarchyConverter;
    private final TimeCalculatorService timeCalculatorService;

    @Autowired
    public TopicThreadServiceImpl(TopicForumRepository topicForumRepository,
                                  TopicThreadRepository topicThreadRepository, PostRepository postRepository,
                                  ForumService forumService, ForumHierarchyConverter forumHierarchyConverter,
                                  TimeCalculatorService timeCalculatorService) {
        this.topicForumRepository = topicForumRepository;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.forumService = forumService;
        this.forumHierarchyConverter = forumHierarchyConverter;
        this.timeCalculatorService = timeCalculatorService;
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
        if (forumService.isForumWithNameExists(forumName) && thread != null && thread.getForum().getName().equals(forumName)) {
            return thread;
        } else {
            return null;
        }
    }

    /**
     * Gets Page number pageNum of Posts belonging to the given TopicThread and sorted by posting date order. The page
     * will contain postsPerPage elements (or less, if its the last page). If the given page number does not exist,
     * returns null
     *
     * @param thread The TopicThread to get posts for
     * @param pageNum the number of the page to get (will be decremented by 1)
     * @param postsPerPage the maximum number of posts per page
     * @return the Page of Posts, or null, if the numbered page does not exist
     */
    @Override
    public Page<Post> getPostPageByThread(TopicThread thread, int pageNum, int postsPerPage) {
        if (pageNum - 1 < 0) {
            System.out.println("### Negative page number");
            return null;
        }

        Pageable pageReq = PageRequest.of(pageNum - 1, postsPerPage, Sort.by("postedAt").ascending());
        Page<Post> postsPage = postRepository.findAllByThread(thread, pageReq);

        if (pageNum > postsPage.getTotalPages()) {
            System.out.println("### Invalid page number");
            return null;
        }

        return postsPage;
    }

    /**
     * Gets Page number pageNum of Posts belonging to the given User and sorted by posting date order. The page
     * will contain postsPerPage elements (or less, if its the last page). If the given page number does not exist,
     * returns null
     *
     * @param user The User to get posts for
     * @param pageNum the number of the page to get (will be decremented by 1)
     * @param postsPerPage the maximum number of posts per page
     * @return the Page of Posts, or null, if the numbered page does not exist
     */
    @Override
    public Page<Post> getPostPageByUser(User user, int pageNum, int postsPerPage) {
        if (pageNum - 1 < 0) {
            System.out.println("### Negative page number");
            return null;
        }

        Pageable pageReq = PageRequest.of(pageNum - 1, postsPerPage, Sort.by("postedAt").descending());
        Page<Post> postsPage = postRepository.findAllByUser(user, pageReq);

        if (postsPage.getTotalElements() == 0) {
            return new PageImpl<Post>(new ArrayList<Post>());
        }

        if (pageNum > postsPage.getTotalPages()) {
            System.out.println("### Invalid page number");
            return null;
        }

        return postsPage;
    }

    /**
     * Helper method that determines the pagination page number (on the user page) of the post with the given ID
     * @param postId
     * @return
     */
    @Override
    public int getPostPageNumberOnThreadByPostId(Long postId) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isEmpty()) {
            return -1;
        }

        Post post = postOpt.get();

        List<Post> sortedPostsOnThread = post.getThread().getPosts().stream().collect(Collectors.toList());
        int postIndex = sortedPostsOnThread.indexOf(post);

        int pageNum = (postIndex / POSTS_PER_PAGE) + 1;

        return pageNum;
    }

    @Override
    public Page<TopicThread> searchTopicThreadsPaginated(String forumName, String searchText) {
        // TODO: For testing
//        PageRequest pageReq = PageRequest.of(0, 1);
//        Page<TopicThread> searchResults = topicThreadRepository.searchForTopicThreadsInForum(forumName, searchText, pageReq);
//        System.out.println("### in searchTopicThread");
//        System.out.println("### searchResults = " + searchResults);
//        System.out.println("### content = " + searchResults.getContent());
//        System.out.println("### page number = " + searchResults.getNumber());
//        System.out.println("### count pages = " + searchResults.getTotalPages());
//        System.out.println("### count elements on page = " + searchResults.getNumberOfElements());
//        System.out.println("### count total elements = " + searchResults.getTotalElements());

        throw new NotYetImplementedException(); // TODO: Implement
    }

    @Override
    public Page<TopicThreadViewDto> searchTopicThreadsAsViewDtos(String forumName, String searchText) {
        throw new NotYetImplementedException(); // TODO: Implement
    }

    /**
     * Searches for all topic threads in a topic forum with the given forumName that have titles that contain all tokens
     * (delimited on double quotes and spaces, but not spaces within double quotes) of the given search text. Empty
     * search text or a search of "" returns all threads in the given forum
     *
     * @param forumName the name of the forum to search
     * @param searchText The text to search for
     * @return the set of TopicThreadViewDtos (ordered reverse chronologically by creation date of the first post) that match
     * the search terms
     * @throws UnsupportedEncodingException
     */
    @Override
    public SortedSet<TopicThreadViewDto> searchTopicThreads(String forumName, String searchText)
            throws UnsupportedEncodingException {
        SortedSet<TopicThread> threads = new TreeSet<>(new Comparator<TopicThread>() {
            @Override
            public int compare(TopicThread o1, TopicThread o2) {
                return o2.getFirstPost().getPostedAt().compareTo(o1.getFirstPost().getPostedAt()); // Newest threads first
            }
        });

        TopicForum forum = topicForumRepository.findByName(forumName);
        if (searchText.equals("") || searchText.equals("\"\"")) {
            if (forum != null) {
                threads.addAll(forum.getThreads());
            }

            return convertThreadsToThreadViewDtos(threads, forum);
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

        return convertThreadsToThreadViewDtos(threads, forum);
    }

    /**
     * Helper method that converts the given threads in the given forum from threads to ThreadViewDtos.
     *
     * @param threads the threads to convert
     * @param forum the forum the threads belong to
     * @return a SortedSet of ThreadViewDtos for the given threads
     */
    private SortedSet<TopicThreadViewDto> convertThreadsToThreadViewDtos(SortedSet<TopicThread> threads, TopicForum forum) {
        SortedSet<TopicThreadViewDto> threadDtos = new TreeSet<>(new Comparator<TopicThreadViewDto>() {
            @Override
            public int compare(TopicThreadViewDto o1, TopicThreadViewDto o2) {
                return o2.getFirstPost().getPostedAt().compareTo(o1.getFirstPost().getPostedAt()); // Newest threads first
            }
        });

        TopicForumViewDto forumViewDto = forumHierarchyConverter.convertForum(forum);
        for (TopicThread thread : threads) {
            TopicThreadViewDto threadDto = forumHierarchyConverter.convertThread(thread, forumViewDto);
            threadDto.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadCreationMessage(threadDto));
            threadDto.setUpdateTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadUpdatedMessage(threadDto));

            threadDtos.add(threadDto);
        }

        return threadDtos;
    }

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
