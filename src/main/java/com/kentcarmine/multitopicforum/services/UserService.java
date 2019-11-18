package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.User;

public interface UserService {
    String getLoggedInUserName();

    User getLoggedInUser();

    User getUser(String name);

    boolean userWithNameExists(String name);

//    void printAuth();
}
