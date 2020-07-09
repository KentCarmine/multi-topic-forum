package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.ForumHierarchyConverter;
import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DuplicateForumNameException;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

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
     * Return a Page that is a slice of all forums as ForumViewDtos sorted in alphabetical order by name (ignoring case)
     *
     * @param pageNum the page number to get
     * @return a Page that is a slice of all forums as ForumViewDtos sorted in alphabetical order by name (ignoring case)
     */
    @Override
    public Page<TopicForumViewDto> getForumsAsViewDtosPaginated(int pageNum, int resultsPerPage) {
        if (pageNum - 1 < 0) {
            return null;
        }

        Pageable pageReq = PageRequest.of(pageNum - 1, resultsPerPage,
                Sort.by(Sort.Order.by("name").ignoreCase()).ascending());
        Page<TopicForum> forumPage = topicForumRepository.findAll(pageReq);

        if (pageNum > forumPage.getTotalPages()) {
            return null;
        }

        List<TopicForumViewDto> forumDtoList = new ArrayList<>();

        for (TopicForum forum : forumPage) {
            TopicForumViewDto forumDto = forumHierarchyConverter.convertForum(forum);
            String mostRecentUpdateMsg = timeCalculatorService.getTimeSinceForumUpdatedMessage(forumDto);
            forumDto.setUpdateTimeDifferenceMessage(mostRecentUpdateMsg);
            forumDtoList.add(forumDto);
        }

        Page<TopicForumViewDto> forumViewDtoPage =
                new PageImpl<TopicForumViewDto>(forumDtoList, pageReq, forumPage.getTotalElements());

        return forumViewDtoPage;
    }

    /**
     * Get the Page of TopicForumViewDtos representing TopicForums that match the given searchText with the given page
     * number, or null if no such page exists.
     *
     * @param searchText the text to search for
     * @param page the number of the page to get
     * @return the Page of TopicForumViewDtos representing TopicForums that match the given searchText with the given
     * page number, or null if no such page exists.
     */
    @Override
    public Page<TopicForumViewDto> searchTopicForumsForViewDtosWithCustomQuery(String searchText, int page, int resultsPerPage) {
        Page<TopicForum> entityResults = searchTopicForumsWithCustomQuery(searchText, page, resultsPerPage);

        if (entityResults == null) {
            return null;
        }

        List<TopicForumViewDto> topicForumViewDtoList = new ArrayList<>();

        for (TopicForum forum : entityResults) {
            TopicForumViewDto dto = forumHierarchyConverter.convertForum(forum);
            String mostRecentUpdateMsg = timeCalculatorService.getTimeSinceForumUpdatedMessage(dto);
            dto.setUpdateTimeDifferenceMessage(mostRecentUpdateMsg);
            topicForumViewDtoList.add(dto);
        }

        Page<TopicForumViewDto> dtoResults = new PageImpl<TopicForumViewDto>(topicForumViewDtoList, entityResults.getPageable(), entityResults.getTotalElements());

        return dtoResults;
    }

    /**
     * Get the Page of TopicForums that match the given searchText with the given page number, or null if no such page
     * exists.
     *
     * @param searchText the text to search for
     * @param page the number of the page to get
     * @return the Page of TopicForums that match the given searchText with the given page number, or null if no such
     * page exists.
     */
    @Override
    public Page<TopicForum> searchTopicForumsWithCustomQuery(String searchText, int page, int resultsPerPage) {
        if (page - 1 < 0) {
            return null;
        }

        PageRequest pageReq = PageRequest.of(page - 1, resultsPerPage, Sort.by(Sort.Order.by("name").ignoreCase()).descending());
        Page<TopicForum> pageResult = topicForumRepository.searchTopicForumsPaginated(searchText, pageReq);

        if (pageResult.getTotalElements() > 0 && page > pageResult.getTotalPages()) {
            return null;
        }

        return pageResult;
    }

    @Override
    public TopicForumViewDtoLight getTopicForumViewDtoLightForTopicForum(TopicForum topicForum) {

        TopicForumViewDtoLight forumViewDto = forumHierarchyConverter.convertForumLight(topicForum);

//        System.out.println("### forumViewDto = " + forumViewDto);

        PostViewDto mostRecentPost = forumViewDto.getMostRecentPost();

//        System.out.println("### mostRecentPost = " + mostRecentPost);

        AbstractTopicThreadViewDto threadViewDto = null;

        if (mostRecentPost != null) {
            threadViewDto = mostRecentPost.getThread();

            threadViewDto.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadCreationMessage(threadViewDto));
            threadViewDto.setUpdateTimeDifferenceMessage(timeCalculatorService.getTimeSinceThreadUpdatedMessage(threadViewDto));

            PostViewDto firstPost = threadViewDto.getFirstPost();
            PostViewDto lastPost = threadViewDto.getLastPost();

            if (firstPost != null) {
                firstPost.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSincePostCreationMessage(firstPost));
            }

            if (lastPost != null) {
                lastPost.setCreationTimeDifferenceMessage(timeCalculatorService.getTimeSincePostCreationMessage(lastPost));
            }
        }

        return forumViewDto;
    }
}
