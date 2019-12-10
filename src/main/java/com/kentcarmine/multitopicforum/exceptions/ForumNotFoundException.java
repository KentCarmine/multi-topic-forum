package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a TopicForum that does not exist.
 */
public class ForumNotFoundException extends ResourceNotFoundException {
    public ForumNotFoundException() {
    }

    public ForumNotFoundException(String message) {
        super(message);
    }

    public ForumNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForumNotFoundException(Throwable cause) {
        super(cause);
    }

    public ForumNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
