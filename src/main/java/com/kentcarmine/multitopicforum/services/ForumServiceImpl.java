package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Service that provides actions related to TopicForums.
 */
@Service
public class ForumServiceImpl implements ForumService {

    private final TopicForumRepository topicForumRepository;
    private final TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;
    private final ForumHierarchyConverter forumHierarchyConverter;
    private final TimeCalculatorService timeCalculatorService;

    @Autowired
    public ForumServiceImpl(TopicForumRepository topicForumRepository,
                            TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter,
                            ForumHierarchyConverter forumHeirarchyConverter,
                            TimeCalculatorService timeCalculatorService) {
        this.topicForumRepository = topicForumRepository;
        this.topicForumDtoToTopicForumConverter = topicForumDtoToTopicForumConverter;
        this.forumHierarchyConverter = forumHeirarchyConverter;
        this.timeCalculatorService = timeCalculatorService;
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
           throw new DuplicateForumNameException();
       }

       return topicForumRepository.save(topicForum);
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
     * Return a SortedSet of all forums as ForumViewDtos sorted in alphabetical order by name
     *
     * @return a SortedSet of all forums  as ForumViewDtos sorted in alphabetical order by name
     */
    @Override
    public SortedSet<TopicForumViewDto> getAllForumsAsViewDtos() {
        SortedSet<TopicForumViewDto> dtos = new TreeSet<>(new Comparator<TopicForumViewDto>() {
            @Override
            public int compare(TopicForumViewDto o1, TopicForumViewDto o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        for (TopicForum forum : getAllForums()) {
            TopicForumViewDto forumDto = forumHierarchyConverter.convertForum(forum);
            String mostRecentUpdateMsg = timeCalculatorService.getTimeSinceForumUpdatedMessage(forumDto);
            forumDto.setUpdateTimeDifferenceMessage(mostRecentUpdateMsg);
            dtos.add(forumDto);
        }

        return dtos;
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

        return forums;
    }

    /**
     * Searches for all topic forums that have names and descriptions that (together) contain all tokens (delimited on
     * double quotes and spaces, but not spaces within double quotes) of the given search text, then returns a SortedSet
     * of TopicForumViewDtos representing those TopicForums
     *
     * @param searchText The text to search for
     * @return the set of TopicForumViewDtos (ordered alphabetically by name) that match the search terms
     * @throws UnsupportedEncodingException
     */
    @Override
    public SortedSet<TopicForumViewDto> searchTopicForumsForViewDtos(String searchText) throws UnsupportedEncodingException {
        SortedSet<TopicForum> forums = searchTopicForums(searchText);

        SortedSet<TopicForumViewDto> forumDtos = new TreeSet<>(new Comparator<TopicForumViewDto>() {
            @Override
            public int compare(TopicForumViewDto o1, TopicForumViewDto o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        for (TopicForum forum : forums) {
            TopicForumViewDto dto = forumHierarchyConverter.convertForum(forum);
            String mostRecentUpdateMsg = timeCalculatorService.getTimeSinceForumUpdatedMessage(dto);
            dto.setUpdateTimeDifferenceMessage(mostRecentUpdateMsg);
            forumDtos.add(dto);
        }

        return forumDtos;
    }

    @Override
    public TopicForumViewDto getTopicForumViewDtoForTopicForum(TopicForum topicForum) {
        TopicForumViewDto forumViewDto = forumHierarchyConverter.convertForum(topicForum);
//        System.out.println("### in getTopicForumViewDtoForTopicForum, starting forumViewDto = " + forumViewDto);

        for (TopicThreadViewDto threadViewDto : forumViewDto.getThreads()) {

            for (PostViewDto postViewDto : threadViewDto.getPosts()) {
                postViewDto.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSincePostCreationMessage(postViewDto));
            }

            threadViewDto.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadCreationMessage(threadViewDto));
            threadViewDto.setUpdateTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadUpdatedMessage(threadViewDto));
        }

        return forumViewDto;
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
}
