package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.dtos.*;
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
        Date currentDate = getCurrentDate();

        TopicThread topicThread = new TopicThread(topicThreadCreationDto.getTitle(), owningForum);
        topicThread.setCreatedAt(currentDate);
//        topicThread.setUpdatedAt(currentDate);
        topicThread = topicThreadRepository.save(topicThread);

        Post post = new Post(topicThreadCreationDto.getFirstPostContent(), currentDate);
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

    /**
     * Get the Page of TopicThreads belonging to the forum with the given name with the given pageNum index that are
     * the results of a search with the given search text, up to a maximum of threadsPerPage
     *
     * @param forumName the forum to search threads for
     * @param searchText the text to search for
     * @param pageNum the number of the page
     * @param threadsPerPage the maximum number of threads per page
     * @return the Page of TopicThreads resulting from the search, or null, if the pageNum was invalid
     */
    @Override
    public Page<TopicThread> searchTopicThreadsPaginated(String forumName, String searchText, int pageNum, int threadsPerPage) {
        if (pageNum - 1 < 0) {
            System.out.println("### Negative page number");
            return null;
        }

        Pageable pageReq = PageRequest.of(pageNum - 1, threadsPerPage);
        Page<TopicThread> threadsPage = topicThreadRepository.searchForTopicThreadsInForum(forumName, searchText, pageReq);

        if (threadsPage.getTotalElements() == 0) {
            return new PageImpl<TopicThread>(new ArrayList<TopicThread>());
        }

        if (pageNum > threadsPage.getTotalPages()) {
            System.out.println("### Invalid page number");
            return null;
        }

        return threadsPage;
    }

    /**
     * Get the Page of TopicThreadViewDtoLight representing TopicThreads belonging to the forum with the given name with
     * the given pageNum index that are the results of a search with the given search text, up to a maximum of
     * threadsPerPage
     *
     * @param forumName the forum to search threads for
     * @param searchText the text to search for
     * @param pageNum the number of the page
     * @param threadsPerPage the maximum number of threads per page
     * @return the Page of TopicThreads resulting from the search, or null, if the pageNum was invalid
     */
    @Override
    public Page<TopicThreadViewDtoLight> searchTopicThreadsAsViewDtos(String forumName, String searchText, int pageNum, int threadsPerPage) {
        Page<TopicThread> threadsPage = searchTopicThreadsPaginated(forumName, searchText, pageNum, threadsPerPage);

        if (threadsPage == null) {
            return null;
        }

        TopicForum forum = forumService.getForumByName(forumName);

        Page<TopicThreadViewDtoLight> threadsDtoPage = convertThreadsToThreadViewDtos(threadsPage, forum);

        return threadsDtoPage;
    }

    /**
     * Get the Page indexed by pageNum consisting of up to threadsPerPage TopicThreads on the given forum.
     *
     * @param forum the forum to get threads for
     * @param pageNum the number of the Page to get
     * @param threadsPerPage the maximum number of threads per page
     * @return the Page indexed by pageNum consisting of up to threadsPerPage TopicThreads on the given forum.
     */
    @Override
    public Page<TopicThread> getTopicThreadsByForumPaginated(TopicForum forum, int pageNum, int threadsPerPage) {
        if (pageNum - 1 < 0) {
            System.out.println("### Negative page number");
            return null;
        }

        Pageable pageReq = PageRequest.of(pageNum - 1, threadsPerPage);
        Page<TopicThread> threadsPage = topicThreadRepository.getAllTopicThreadsPaginated(forum.getName(), pageReq);

        if (threadsPage.getTotalElements() == 0) {
            return new PageImpl<TopicThread>(new ArrayList<TopicThread>());
        }

        if (pageNum > threadsPage.getTotalPages()) {
            System.out.println("### Invalid page number");
            return null;
        }

        return threadsPage;
    }

    /**
     * Get the Page indexed by pageNum consisting of up to threadsPerPage TopicThreadViewDtoLights representing
     * TopicThreads on the given forum.
     *
     * @param forum the forum to get threads for
     * @param pageNum the number of the Page to get
     * @param threadsPerPage the maximum number of threads per page
     * @return the Page indexed by pageNum consisting of up to threadsPerPage TopicThreadViewDtoLights representing
     * TopicThreads on the given forum.
     */
    @Override
    public Page<TopicThreadViewDtoLight> getTopicThreadViewDtosLightByForumPaginated(TopicForum forum, int pageNum, int threadsPerPage) {
        Page<TopicThread> threads = getTopicThreadsByForumPaginated(forum, pageNum, threadsPerPage);

        if (threads == null) {
            return null;
        }

        return convertThreadsToThreadViewDtos(threads, forum);
    }

    /**
     * Helper method that converts a Page of TopicThreads from the given forum into a Page of TopicThreadViewDtoLights
     * representing those TopicThreads
     *
     * @param threads the Page of TopicThreads to convert
     * @param forum the forum the TopicThreads belong to
     * @return the page of TopicThreadViewDtoLights representing threads
     */
    private Page<TopicThreadViewDtoLight> convertThreadsToThreadViewDtos(Page<TopicThread> threads, TopicForum forum) {
        TopicForumViewDtoLight forumViewDto = forumHierarchyConverter.convertForumLight(forum);

        List<TopicThreadViewDtoLight> threadDtos = new ArrayList<>();

        for (TopicThread thread : threads) {
            TopicThreadViewDtoLight threadDto = forumHierarchyConverter.convertThreadLight(thread, forumViewDto);
            threadDto.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadCreationMessage(threadDto));
            threadDto.setUpdateTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadUpdatedMessage(threadDto));

            threadDtos.add(threadDto);
        }

        Page<TopicThreadViewDtoLight> threadDtoPage = new PageImpl<TopicThreadViewDtoLight>(threadDtos, threads.getPageable(), threads.getTotalElements());

        return threadDtoPage;
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
        boolean userOutranksThreadLocker = thread.getLockingUser() != null && (user.equals(thread.getLockingUser()) || user.isHigherAuthority(thread.getLockingUser()));

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
