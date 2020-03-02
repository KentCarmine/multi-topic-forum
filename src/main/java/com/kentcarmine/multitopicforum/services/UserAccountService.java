package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.VerificationToken;

import java.util.Locale;

public interface UserAccountService {
    User createUserByUserDto(UserDto userDto) throws DuplicateEmailException, DuplicateUsernameException;

    User createUser(User user) throws DuplicateEmailException, DuplicateUsernameException;

    User getUserByVerificationToken(String verificationToken);

    VerificationToken getVerificationToken(String token);

    void saveRegisteredUser(User user);

    void createVerificationToken(User user, String token);

    VerificationToken generateNewVerificationToken(String existingToken);

    boolean isVerificationTokenExpired(VerificationToken verificationToken);

    PasswordResetToken createPasswordResetTokenForUser(User user);

    boolean validatePasswordResetToken(User user, String token);

    void changeUserPassword(User user, String newPassword);

    String getInvalidAuthTokenMessage(Locale locale);

    String getExpiredAuthTokenMessage(Locale locale);

    String getPasswordResetEmailContent(String resetUrl, Locale locale);

    String getResendVerificationTokenEmailContent(String confirmationUrl, Locale locale);

    String getAuthenticationFailureMessage(Exception authException, Locale locale);


}
