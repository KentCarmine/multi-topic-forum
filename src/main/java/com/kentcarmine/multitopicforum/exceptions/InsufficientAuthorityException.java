package com.kentcarmine.multitopicforum.exceptions;

public class InsufficientAuthorityException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Insufficient authority to perform that action.";

    public InsufficientAuthorityException() {
        super(DEFAULT_MESSAGE);
    }

    public InsufficientAuthorityException(String message) {
        super(message);
    }

    public InsufficientAuthorityException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientAuthorityException(Throwable cause) {
        super(cause);
    }

    protected InsufficientAuthorityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
