package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.User;


public interface UserService {
    String getLoggedInUserName();

    User getLoggedInUser();

    User getUser(String name);

    User createUserByUserDto(UserDto userDto) throws DuplicateEmailException, DuplicateUsernameException;

    User createUser(User user) throws DuplicateEmailException, DuplicateUsernameException;

    boolean emailExists(String email);

    boolean usernameExists(String username);

    boolean isUserLoggedIn();

//    void printAuth();
}
