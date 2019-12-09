package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to save a TopicForum object with a name that already exists.
 */
public class DuplicateForumNameException extends RuntimeException {

    public DuplicateForumNameException() {
    }

    public DuplicateForumNameException(String message) {
        super(message);
    }

    public DuplicateForumNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateForumNameException(Throwable cause) {
        super(cause);
    }

    public DuplicateForumNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
