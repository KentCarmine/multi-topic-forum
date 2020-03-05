package com.kentcarmine.multitopicforum.exceptions;

/**
 * Exception thrown when attempting to access a User that does not exist.
 */
public class UserNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE_CODE_NULL_USERNAME = "Exception.user.notfound.usernameNull";

    private String username;

    public UserNotFoundException() {
        super(DEFAULT_MESSAGE_CODE_NULL_USERNAME);
    }

    public UserNotFoundException(String message, String username) {
        super(message);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
