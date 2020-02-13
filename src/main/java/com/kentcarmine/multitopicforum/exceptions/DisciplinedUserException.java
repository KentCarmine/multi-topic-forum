package com.kentcarmine.multitopicforum.exceptions;

import com.kentcarmine.multitopicforum.model.User;

/**
 * Exception thrown when attempting to take actions requiring authentication as a currently banned or suspended user.
 */
public class DisciplinedUserException extends RuntimeException {

    private User user;

    public DisciplinedUserException(User user) {
        super("DisciplinedUserException: User " + user.getUsername() + " is currently banned or suspended.");
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "DisciplinedUserException{" +
                "user=" + user +
                '}';
    }
}
