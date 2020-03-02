package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.User;

/**
 * Interface for Services that handle authentication
 */
public interface AuthenticationService {
    String getLoggedInUserName();

    boolean isUserLoggedIn();

    void updateAuthorities(User loggedInUser);
}
