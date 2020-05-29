package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a Pagination Page that does not exist.
 */
public class PageNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE_CODE = "Exception.paginationPage.notfound";

    public PageNotFoundException() {
        super(DEFAULT_MESSAGE_CODE);
    }

    public PageNotFoundException(String message) {
        super(message);
    }

}
