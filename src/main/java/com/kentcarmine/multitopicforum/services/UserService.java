package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
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

    boolean isValidPromotionRequest(User loggedInUser, User userToPromote, UserRole promotedRank);

    User promoteUser(User userToPromote);

    boolean isValidDemotionRequest(User loggedInUser, User userToDemote, UserRole demotedRank);

    User demoteUser(User userToDemote);

    boolean disciplineUser(UserDisciplineSubmissionDto userDisciplineSubmissionDto, User loggedInUser);

    void forceLogOut(User loggedInUser, HttpServletRequest httpServletRequest, HttpServletResponse res);

    void handleDisciplinedUser(User user);

    SortedSet<DisciplineViewDto> getActiveDisciplinesForUser(User user, User loggedInUser);

    SortedSet<DisciplineViewDto> getInactiveDisciplinesForUser(User user);

    Discipline getDisciplineByIdAndUser(Long id, User user);

    void rescindDiscipline(Discipline disciplineToRescind);

    UserRankAdjustmentDto getUserRankAdjustmentDtoForUser(User user, User loggedInUser);

    boolean isVerificationTokenExpired(VerificationToken verificationToken);

    String getExpiredAuthTokenMessage(Locale locale);

    String getInvalidAuthTokenMessage(Locale locale);

    String getLoggedInUserBannedInformationMessage(Discipline greatestDurationActiveDiscipline);

    String getPasswordResetEmailContent(String resetUrl, Locale locale);

    String getResendVerificationTokenEmailContent(String confirmationUrl, Locale locale);

    String getAuthenticationFailureMessage(Exception authException, Locale locale);

    PromoteUserResponseDto getPromoteUserResponseDtoForUser(User promotedUser);

    DemoteUserResponseDto getDemoteUserResponseDtoForUser(User demotedUser);

    User getLoggedInUserIfNotDisciplined();
}
