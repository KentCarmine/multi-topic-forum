package com.kentcarmine.multitopicforum.exceptions;

public class InsufficientAuthorityException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Exception.authority.insufficient";

    public InsufficientAuthorityException() {
        super(DEFAULT_MESSAGE);
    }

    public InsufficientAuthorityException(String message) {
        super(message);
    }
}
