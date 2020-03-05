package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a TopicForum that does not exist.
 */
public class ForumNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE_CODE = "Exception.forum.notfound";

    public ForumNotFoundException() {
        super(DEFAULT_MESSAGE_CODE);
    }

    public ForumNotFoundException(String message) {
        super(message);
    }

}
