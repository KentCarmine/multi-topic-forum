package com.kentcarmine.multitopicforum.services;

/**
 * Interface for Services that handle authentication
 */
public interface AuthenticationService {
    String getLoggedInUserName();

    boolean isUserLoggedIn();

    void debugPrintAuthorities();
}
