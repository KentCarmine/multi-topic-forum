package com.kentcarmine.multitopicforum.services;


public interface AuthenticationService {
    String getLoggedInUserName();

    boolean isUserLoggedIn();

    void debugPrintAuthorities();
}
