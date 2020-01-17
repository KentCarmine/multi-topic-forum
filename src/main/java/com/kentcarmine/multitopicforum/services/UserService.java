package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.VerificationToken;

import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

/**
 * Specification for services that provide actions related to Users
 */
public interface UserService {

    User getLoggedInUser();

    User getUser(String name);

    User getUserByEmail(String email);

    User createUserByUserDto(UserDto userDto) throws DuplicateEmailException, DuplicateUsernameException;

    User createUser(User user) throws DuplicateEmailException, DuplicateUsernameException;

    User getUserByVerificationToken(String token);

    VerificationToken getVerificationToken(String verificationToken);

    VerificationToken generateNewVerificationToken(String existingToken);

    PasswordResetToken createPasswordResetTokenForUser(User user);

    boolean validatePasswordResetToken(User user, String token);

    void changeUserPassword(User user, String newPassword);

    void saveRegisteredUser(User user);

    void createVerificationToken(User user, String token);

    boolean emailExists(String email);

    boolean usernameExists(String username);

    SortedSet<User> searchForUsers(String searchText) throws UnsupportedEncodingException;

    SortedSet<String> searchForUsernames(String searchText) throws UnsupportedEncodingException;
}
