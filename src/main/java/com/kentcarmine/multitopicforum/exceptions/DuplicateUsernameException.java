package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to save a User object with a username that already exists.
 */
public class DuplicateUsernameException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User.username.duplicate";

    public DuplicateUsernameException() {
        super(DEFAULT_MESSAGE);
    }

    public DuplicateUsernameException(String message) {
        super(message);
    }

}
