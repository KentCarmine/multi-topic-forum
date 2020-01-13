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
     * Return a SortedSet of all forums sorted in alphabetical order by name
     *
     * @return a SortedSet of all forums sorted in alphabetical order by name
     */
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
     * Helper method that parses text entered into a search field. The string is split on spaces and double quotes, with
     * substrings inside double quotes still containing spaces. Returns a list of tokens.
     *
     * @param searchText the text to be parsed.
     * @return the list of tokens
     * @throws UnsupportedEncodingException
     */
    private List<String> parseSearchText(String searchText) throws UnsupportedEncodingException {
        List<String> searchTerms = new ArrayList<>();

        searchText = decodeUrl(searchText).trim();

        String regex = "\"([^\"]*)\"|(\\S+)";

        Matcher matcher = Pattern.compile(regex).matcher(searchText);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
//                System.out.println(matcher.group(1));
                searchTerms.add(matcher.group(1));
            } else {
//                System.out.println(matcher.group(2));
                searchTerms.add(matcher.group(2));
            }
        }

        return searchTerms.stream().filter(st -> st.length() > 0).distinct().collect(Collectors.toList());
    }

    /**
     * Helper method that decodes a URL-safe encoded string
     * @param value the string to decode
     * @return the decodes tring
     * @throws UnsupportedEncodingException
     */
    private String decodeUrl(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
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
