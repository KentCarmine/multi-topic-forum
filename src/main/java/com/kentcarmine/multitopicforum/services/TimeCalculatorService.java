package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.PostViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;

/**
 * Interface that defines a service that provides methods related to timing of thread and post updates and creations.
 */
public interface TimeCalculatorService {
    String getTimeSincePostCreationMessage(PostViewDto postViewDto);

    String getTimeSinceThreadCreationMessage(TopicThreadViewDto threadViewDto);

    String getTimeSinceThreadUpdatedMessage(TopicThreadViewDto threadViewDto);
}
