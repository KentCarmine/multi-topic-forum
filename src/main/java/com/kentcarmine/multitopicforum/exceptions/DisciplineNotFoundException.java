package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a Discipline that does not exist.
 */
public class DisciplineNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "Discipline was not found";

    public DisciplineNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public DisciplineNotFoundException(String message) {
        super(message);
    }

    public DisciplineNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisciplineNotFoundException(Throwable cause) {
        super(cause);
    }

    public DisciplineNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
