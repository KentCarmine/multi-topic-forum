package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a TopicThread that does not exist.
 */
public class TopicThreadNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE_CODE = "Exception.thread.notfound";

    public TopicThreadNotFoundException() {
        super(DEFAULT_MESSAGE_CODE);
    }

    public TopicThreadNotFoundException(String message) {
        super(message);
    }
}
