package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a TopicThread that does not exist.
 */
public class TopicThreadNotFoundException extends ResourceNotFoundException {

    public TopicThreadNotFoundException() {
    }

    public TopicThreadNotFoundException(String message) {
        super(message);
    }

    public TopicThreadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicThreadNotFoundException(Throwable cause) {
        super(cause);
    }

    public TopicThreadNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
