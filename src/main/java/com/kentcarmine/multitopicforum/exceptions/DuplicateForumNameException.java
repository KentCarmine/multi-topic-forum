package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to save a TopicForum object with a name that already exists.
 */
public class DuplicateForumNameException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Forum.creation.duplicateName";

    public DuplicateForumNameException() {
        super(DEFAULT_MESSAGE);
    }

    public DuplicateForumNameException(String message) {
        super(message);
    }
}
