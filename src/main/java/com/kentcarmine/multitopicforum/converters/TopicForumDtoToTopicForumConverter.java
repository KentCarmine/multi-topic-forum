package com.kentcarmine.multitopicforum.converters;

import com.kentcarmine.multitopicforum.dtos.TopicForumDto;
import com.kentcarmine.multitopicforum.model.TopicForum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter object that converts TopicForumDto objects to TopicForum objects
 */
@Component
public class TopicForumDtoToTopicForumConverter implements Converter<TopicForumDto, TopicForum> {

    @Override
    public TopicForum convert(TopicForumDto topicForumDto) {
        TopicForum topicForum = new TopicForum(topicForumDto.getName(), topicForumDto.getDescription());

        return topicForum;
    }
}
