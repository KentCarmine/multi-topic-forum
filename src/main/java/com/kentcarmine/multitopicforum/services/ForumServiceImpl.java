package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.helpers.URLEncoderDecoderHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.PostVoteRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that provides actions related to TopicForums.
 */
@Service
public class ForumServiceImpl implements ForumService {

    private final TopicForumRepository topicForumRepository;
    private final TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;
    private final TopicThreadRepository topicThreadRepository;
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;

    @Autowired
    public ForumServiceImpl(TopicForumRepository topicForumRepository,
                            TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter,
                            TopicThreadRepository topicThreadRepository, PostRepository postRepository,
                            PostVoteRepository postVoteRepository) {
        this.topicForumRepository = topicForumRepository;
        this.topicForumDtoToTopicForumConverter = topicForumDtoToTopicForumConverter;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
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
     * Return a SortedSet of all forums sorted in alphabetical order by name
     *
     * @return a SortedSet of all forums sorted in alphabetical order by name
     */
    @Override
    public SortedSet<TopicForum> getAllForums() {
        SortedSet<TopicForum> forums = new TreeSet<>(new Comparator<TopicForum>() {
            @Override
            public int compare(TopicForum o1, TopicForum o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        topicForumRepository.findAll().forEach(forums::add);

        return forums;
    }

    /**
     * Searches for all topic forums that have names and descriptions that (together) contain all tokens (delimited on
     * double quotes and spaces, but not spaces within double quotes) of the given search text.
     *
     * @param searchText The text to search for
     * @return the set of TopicForums (ordered alphabetically) that match the search terms
     * @throws UnsupportedEncodingException
     */
    @Override
    public SortedSet<TopicForum> searchTopicForums(String searchText) throws UnsupportedEncodingException {
        SortedSet<TopicForum> forums = new TreeSet<>(new Comparator<TopicForum>() {
            @Override
            public int compare(TopicForum o1, TopicForum o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        List<String> searchTerms = parseSearchText(searchText);
        List<List<TopicForum>> searchTermResults = new ArrayList<>();
        for (int i = 0; i < searchTerms.size(); i++) {
            searchTermResults.add(new ArrayList<TopicForum>());
        }

//        System.out.println("### SEARCH TERMS:");
//        for (String st: searchTerms) {
//            System.out.println(st);
//        }
//        System.out.println("### END SEARCH TERMS");

        for(int i = 0; i < searchTerms.size(); i++) {
            String st = searchTerms.get(i);
            searchTermResults.set(i, topicForumRepository.findByNameLikeIgnoreCaseOrDescriptionLikeIgnoreCase("%" + st + "%", "%" + st + "%"));
        }

        if (!searchTermResults.isEmpty()) {
            forums.addAll(searchTermResults.get(0));
            searchTermResults.remove(0);
            for (List<TopicForum> str : searchTermResults) {
                forums.retainAll(str);
            }
        }

//        System.out.println("### SEARCH RESULTS:");
//        for (TopicForum f : forums) {
//            System.out.println(f.getName());
//        }
//        System.out.println("### END SEARCH RESULTS");

        return forums;
    }

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

    /**
     * Generates a map from Post IDs to votes made on those posts by the given user. Those values can be 1 (upvote),
     * 0 (no vote), or -1 (downvote).
     *
     * @param loggedInUser the user to check votes made by
     * @param thread the TopicThread to get the list of post IDs from
     * @return map from Post IDs to votes made on those posts by the given user
     */
    @Override
    public Map<Long, Integer> generateVoteMap(User loggedInUser, TopicThread thread) {
        Map<Long, Integer> voteMap = new HashMap<>();

        for (Post post : thread.getPosts()) {
            PostVote vote = postVoteRepository.findByUserAndPost(loggedInUser, post);
            if (vote == null) {
                voteMap.put(post.getId(), PostVoteState.NONE.getValue());
            } else {
                voteMap.put(post.getId(), vote.getPostVoteState().getValue());
            }
        }

        return voteMap;
    }

    @Override
    public Post getPostById(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()) {
            return null;
        } else {
            return postOpt.get();
        }
    }

    /**
     * Get the PostVote made by the given user on the given post, or null if no such PostVote exists.
     * @param user the user owning the PostVote
     * @param post the post owning the PostVote
     * @return the PostVote made by the given user on the given post, or null if no such PostVote exists.
     */
    @Override
    public PostVote getPostVoteByUserAndPost(User user, Post post) {
        return postVoteRepository.findByUserAndPost(user, post);
    }

    /**
     * Processes submission of a PostVote by the given user on the given post with vote values in the
     * postVoteSubmissionDto. Either creates a new vote if no vote by that user on that post exists, or updates that
     * user's existing vote on that post if it has a value of NONE. Then returns data to the client indicating the
     * current number of votes on that post and if the user's vote was saved.
     *
     * @param loggedInUser The user submitting the vote
     * @param post The post the vote is on
     * @param postVoteSubmissionDto data about the vote
     * @return the response object to be sent back to the client
     */
    @Transactional
    @Override
    public PostVoteResponseDto handlePostVoteSubmission(User loggedInUser, Post post, PostVoteSubmissionDto postVoteSubmissionDto) {
        PostVoteResponseDto postVoteResponseDto;

        PostVote postVote = getPostVoteByUserAndPost(loggedInUser, post);
        if (postVote == null || postVote.getPostVoteState().equals(PostVoteState.NONE)) {
            if (postVote == null) {
                System.out.println("### Creating new vote");
                postVote = new PostVote(PostVoteState.NONE, loggedInUser, post);
            } else {
                System.out.println("### Updating existing vote");
            }

            PostVoteState voteState;
            if (postVoteSubmissionDto.getVoteValue() == 1) {
                voteState = PostVoteState.UPVOTE;
            } else if (postVoteSubmissionDto.getVoteValue() == -1) {
                voteState = PostVoteState.DOWNVOTE;
            } else {
                voteState = PostVoteState.NONE;
            }
            postVote.setPostVoteState(voteState);
            postVote = postVoteRepository.save(postVote);
            post.addPostVote(postVote);

            postVoteResponseDto = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), true, post.getVoteCount());
//            System.out.println("### Response: " + postVoteResponseDto);
        } else {
            System.out.println("### Invalid vote submission in handlePostVoteSubmission()");
            postVoteResponseDto = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), false, post.getVoteCount());
        }

        return postVoteResponseDto;
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
