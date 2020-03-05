package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to save a User object with an email that already exists.
 */
public class DuplicateEmailException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User.email.duplicate";

    public DuplicateEmailException() {
        super(DEFAULT_MESSAGE);
    }

    public DuplicateEmailException(String message) {
        super(message);
    }
}
