package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.User;
import org.springframework.security.core.Authentication;

/**
 * Interface for Services that handle authentication
 */
public interface AuthenticationService {
    String getLoggedInUserName();

    boolean isUserLoggedIn();

    void debugPrintAuthorities();

    void updateAuthorities(User loggedInUser);
}
