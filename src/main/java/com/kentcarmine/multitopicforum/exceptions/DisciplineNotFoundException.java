package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a Discipline that does not exist.
 */
public class DisciplineNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "Exception.discipline.notFound";

    public DisciplineNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public DisciplineNotFoundException(String message) {
        super(message);
    }
}
