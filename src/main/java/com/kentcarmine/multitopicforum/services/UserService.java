package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.VerificationToken;


public interface UserService {

    User getLoggedInUser();

    User getUser(String name);

    User createUserByUserDto(UserDto userDto) throws DuplicateEmailException, DuplicateUsernameException;

    User createUser(User user) throws DuplicateEmailException, DuplicateUsernameException;

    User getUserByVerificationToken(String token);

    VerificationToken getVerificationToken(String VerificationToken);

    void saveRegisteredUser(User user);

    void createVerificationToken(User user, String token);

    boolean emailExists(String email);

    boolean usernameExists(String username);
}
