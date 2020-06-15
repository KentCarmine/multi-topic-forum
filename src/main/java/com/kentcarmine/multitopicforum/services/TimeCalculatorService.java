package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.AbstractTopicThreadViewDto;
import com.kentcarmine.multitopicforum.dtos.PostViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
import com.kentcarmine.multitopicforum.model.User;

/**
 * Interface that defines a service that provides methods related to timing of thread and post updates and creations
 * and User activity.
 */
public interface TimeCalculatorService {
    String getTimeSincePostCreationMessage(PostViewDto postViewDto);

    String getTimeSinceThreadCreationMessage(AbstractTopicThreadViewDto threadViewDto);

    String getTimeSinceThreadUpdatedMessage(AbstractTopicThreadViewDto threadViewDto);

    String getTimeSinceForumUpdatedMessage(TopicForumViewDto topicForumViewDto);

    String getTimeSinceUserLastActiveMessage(User user);
}
