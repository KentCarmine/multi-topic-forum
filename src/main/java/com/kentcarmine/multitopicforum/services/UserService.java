package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.DemoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.PromoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.dtos.UserSearchResultDto;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

/**
 * Specification for services that provide actions related to Users
 */
public interface UserService {

    User getLoggedInUser();

    User getUser(String name);

    User getUserByEmail(String email);

    boolean emailExists(String email);

    boolean usernameExists(String username);

//    SortedSet<User> searchForUsers(String searchText) throws UnsupportedEncodingException;
//
//    SortedSet<String> searchForUsernames(String searchText) throws UnsupportedEncodingException;

    SortedSet<UserSearchResultDto> searchForUsernames(String searchText) throws UnsupportedEncodingException;

    SortedSet<UserSearchResultDto> searchForUsers(String searchText) throws UnsupportedEncodingException;

    boolean isValidPromotionRequest(User loggedInUser, User userToPromote, UserRole promotedRank);

    User promoteUser(User userToPromote);

    boolean isValidDemotionRequest(User loggedInUser, User userToDemote, UserRole demotedRank);

    User demoteUser(User userToDemote);

    void forceLogOut(User loggedInUser, HttpServletRequest httpServletRequest, HttpServletResponse res);

    UserRankAdjustmentDto getUserRankAdjustmentDtoForUser(User user, User loggedInUser);

    PromoteUserResponseDto getPromoteUserResponseDtoForUser(User promotedUser);

    DemoteUserResponseDto getDemoteUserResponseDtoForUser(User demotedUser);

    User getLoggedInUserIfNotDisciplined();
}
