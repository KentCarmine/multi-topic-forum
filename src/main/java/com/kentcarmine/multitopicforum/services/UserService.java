package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.DemoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.PromoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.dtos.UserSearchResultDto;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import org.springframework.data.domain.Page;

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

    Page<User> searchForUsersPaginated(String searchText, int pageNum, int usersPerPage);

    Page<UserSearchResultDto> searchForUserDtosPaginated(String searchText, int pageNum, int usersPerPage);

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
