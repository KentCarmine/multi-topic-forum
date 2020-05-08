package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DemoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.PromoteUserResponseDto;
import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.dtos.UserSearchResultDto;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.AuthorityRepository;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final AuthorityRepository authorityRepository;
    private final UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;
    private final MessageService messageService;
    private final TimeCalculatorService timeCalculatorService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService,
                           AuthorityRepository authorityRepository,
                           UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter,
                           MessageService messageService, TimeCalculatorService timeCalculatorService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.authorityRepository = authorityRepository;
        this.userToUserRankAdjustmentDtoConverter = userToUserRankAdjustmentDtoConverter;
        this.messageService = messageService;
        this.timeCalculatorService = timeCalculatorService;
    }

    /**
     * Gets the currently logged in user
     *
     * @return the currently logged in user. Can be ANONYMOUS
     */
    @Override
    public User getLoggedInUser() {
        User loggedInUser = getUser(authenticationService.getLoggedInUserName());
        authenticationService.updateAuthorities(loggedInUser);

        return loggedInUser;
    }

    /**
     * Get a User by username
     *
     * @param name the name of the user to get
     * @return the user with the given username, or null if no such user exists
     */
    @Override
    public User getUser(String name) {
        if (name == null) {
            return null;
        }

        return userRepository.findByUsername(name);
    }

    /**
     * Get a User by email
     *
     * @param email the email of the user to get
     * @return the user with the given email, or null if no such user exists
     */
    @Override
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email);
    }

    /**
     * Check if any user with the given email exists in persistent storage.
     *
     * @param email the email to check for
     * @return true if a user with the given email exists, false otherwise
     */
    @Override
    public boolean emailExists(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    /**
     * Check if any user with the given username exists in persistent storage.
     *
     * @param username the username to check for
     * @return true if a user with the given username exists, false otherwise
     */
    @Override
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

//    /**
//     * Searches for the names of all Users that have names that contain all tokens (delimited on double quotes and
//     * spaces, but not spaces within double quotes) of the given search text.
//     *
//     * @param searchText The text to search for
//     * @return the set of usernames of Users (ordered alphabetically) that match the search terms
//     * @throws UnsupportedEncodingException
//     */
//    public SortedSet<String> searchForUsernames(String searchText) throws UnsupportedEncodingException {
//        SortedSet<User> users = searchForUsers(searchText);
//        SortedSet<String> usernames = new TreeSet<>(users.stream().map(User::getUsername).collect(Collectors.toList()));
//        return usernames;
//    }

    @Override
    public SortedSet<UserSearchResultDto> searchForUsernames(String searchText) throws UnsupportedEncodingException {
        SortedSet<UserSearchResultDto> users = searchForUsers(searchText);

        return users;
    }

    /**
     * Searches for all Users that have names that contain all tokens (delimited on double quotes and spaces, but not
     * spaces within double quotes) of the given search text.
     *
     * @param searchText The text to search for
     * @return the set of Users (ordered alphabetically) that match the search terms
     * @throws UnsupportedEncodingException
     */
    @Override
    public SortedSet<UserSearchResultDto> searchForUsers(String searchText) throws UnsupportedEncodingException {
        SortedSet<UserSearchResultDto> users = new TreeSet<>();

        List<String> searchTerms = parseSearchText(searchText);
        List<List<UserSearchResultDto>> searchTermResults = new ArrayList<>();
        for (int i = 0; i < searchTerms.size(); i++) {
            searchTermResults.add(new ArrayList<UserSearchResultDto>());
        }

        for(int i = 0; i < searchTerms.size(); i++) {
            String st = searchTerms.get(i);

            List<User> usrRes = userRepository.findByUsernameLikeIgnoreCase("%" + st + "%");
            searchTermResults.set(i, convertUserListToUserSearchResultDto(usrRes));
        }

        if (!searchTermResults.isEmpty()) {
            users.addAll(searchTermResults.get(0));
            searchTermResults.remove(0);

            for (List<UserSearchResultDto> str : searchTermResults) {
                users.retainAll(str);
            }
        }

        return users;
    }



//    /**
//     * Searches for all Users that have names that contain all tokens (delimited on double quotes and spaces, but not
//     * spaces within double quotes) of the given search text.
//     *
//     * @param searchText The text to search for
//     * @return the set of Users (ordered alphabetically) that match the search terms
//     * @throws UnsupportedEncodingException
//     */
//    @Override
//    public SortedSet<User> searchForUsers(String searchText) throws UnsupportedEncodingException {
//        SortedSet<User> users = new TreeSet<>((o1, o2) -> o1.getUsername().toLowerCase().compareTo(o2.getUsername().toLowerCase()));
//
//        List<String> searchTerms = parseSearchText(searchText);
//        List<List<User>> searchTermResults = new ArrayList<>();
//        for (int i = 0; i < searchTerms.size(); i++) {
//            searchTermResults.add(new ArrayList<User>());
//        }
//
//        for(int i = 0; i < searchTerms.size(); i++) {
//            String st = searchTerms.get(i);
//            searchTermResults.set(i, userRepository.findByUsernameLikeIgnoreCase("%" + st + "%"));
//        }
//
//        if (!searchTermResults.isEmpty()) {
//            users.addAll(searchTermResults.get(0));
//            searchTermResults.remove(0);
//            for (List<User> str : searchTermResults) {
//                users.retainAll(str);
//            }
//        }
//
//        return users;
//    }

    /**
     * Promotes the given user by one rank, then saves and returns that user.
     *
     * @param userToPromote the user to be promoted
     * @return the updated user
     */
    @Override
    public User promoteUser(User userToPromote) {
        userToPromote.addAuthority(userToPromote.getIncrementedRank());
        return userRepository.save(userToPromote);
    }

    /**
     * Checks if the logged in user can promote the userToPromote to the promoted rank. The logged in user must be
     * higher ranked than the rank they are promoting userToPromote to, and userToPromote must not be a superadmin or
     * be promoted to superadmin.
     *
     * @param loggedInUser the logged in (promoting) user
     * @param userToPromote the user to be promoted
     * @param promotedRank the rank to promote to
     * @return true if the promotion request is valid, false otherwise
     */
    @Override
    public boolean isValidPromotionRequest(User loggedInUser, User userToPromote, UserRole promotedRank) {
        if(promotedRank == null || loggedInUser == null || userToPromote == null) {
            return false;
        }

        if (userToPromote.getHighestAuthority().equals(UserRole.SUPER_ADMINISTRATOR)
                || userToPromote.getIncrementedRank().equals(UserRole.SUPER_ADMINISTRATOR)
                || promotedRank.equals(UserRole.SUPER_ADMINISTRATOR)) {
            return false;
        }

        if (loggedInUser.getHighestAuthority().isHigherRank(userToPromote.getIncrementedRank())
                && loggedInUser.getHighestAuthority().isHigherRank(promotedRank)
                && userToPromote.getIncrementedRank().equals(promotedRank)) {
            return true;
        }

        return false;
    }

    /**
     * Demotes the given user by one rank, then saves and returns that user.
     *
     * @param userToDemote the user to be demoted
     * @return the updated user
     */
    @Transactional
    @Override
    public User demoteUser(User userToDemote) {
        UserRole roleToRemove = userToDemote.getHighestAuthority();
        userToDemote.removeAuthority(roleToRemove);
        authorityRepository.deleteByUserAndAuthority(userToDemote, roleToRemove);
        return userRepository.save(userToDemote);
    }

    /**
     * Checks if the logged in user can demote the userToDemote to the demoted rank. The logged in user must be
     * higher ranked than the userToDemote, userToDemote must not be of the lowest possible rank, and userToDemote's
     * next lowest rank and demotedRank must match.
     *
     * @param loggedInUser the logged in (demoting) user
     * @param userToDemote the user to be demoted
     * @param demotedRank the rank to demote to
     * @return true if the demotion request is valid, false otherwise
     */
    @Override
    public boolean isValidDemotionRequest(User loggedInUser, User userToDemote, UserRole demotedRank) {
        if(demotedRank == null || loggedInUser == null || userToDemote == null) {
            return false;
        }

        if (userToDemote.getDecrementedRank() == null || !userToDemote.getDecrementedRank().equals(demotedRank)) {
            return false;
        }

        if (userToDemote.isDemotableBy(loggedInUser) ) {
            return true;
        }

        return false;
    }

    /**
     * Forcibly logs out the currently logged in user and clears their remember me cookie
     *
     * @param loggedInUser the logged in user
     * @param req the HTTPServletRequest to call logout on
     * @param res the HTTPServletResponse to use to set cleared remember me cookie on
     */
    @Override
    public void forceLogOut(User loggedInUser, HttpServletRequest req, HttpServletResponse res) {
//        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        new SecurityContextLogoutHandler().logout(req, null, null);
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken("anonymous", "anonymous",
                new ArrayList(Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        SecurityContextHolder.getContext().setAuthentication(anonymous);
        cancelRememberMeCookie(req, res);
    }

    /**
     * Helper method that clears the user's remember me cookie (if any)
     */
    private void cancelRememberMeCookie(HttpServletRequest req, HttpServletResponse res) {
        String cookieName = AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath(StringUtils.hasLength(req.getContextPath()) ? req.getContextPath() : "/");
        res.addCookie(cookie);
    }

    /**
     * Generates and returns a UserRankAdjustmentDto for the given User, assuming the given loggedInUser.
     *
     * @param user the user to generate the DTO for
     * @param loggedInUser the logged in user
     * @return a UserRankAdjustmentDto for the given User, assuming the given loggedInUser
     */
    @Override
    public UserRankAdjustmentDto getUserRankAdjustmentDtoForUser(User user, User loggedInUser) {
        UserRankAdjustmentDto userRankAdjustmentDto = userToUserRankAdjustmentDtoConverter.convert(user);
        userRankAdjustmentDto.setDemotableByLoggedInUser(user.isDemotableBy(loggedInUser));
        userRankAdjustmentDto.setPromotableByLoggedInUser(user.isPromotableBy(loggedInUser));

        return userRankAdjustmentDto;
    }

    /**
     * Get a PromoteUserResponseDto for the given user which contains information about a successful promotion.
     *
     * @param promotedUser the promoted user
     * @return the PromoteUserResponseDto for the given user which contains information about a successful promotion
     */
    @Override
    public PromoteUserResponseDto getPromoteUserResponseDtoForUser(User promotedUser) {
        String msg = messageService.getMessage("User.authority.promotion.notification", promotedUser.getUsername(),
                promotedUser.getHighestAuthority().getDisplayRank());

        String newPromoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/promoteUserButton/" + promotedUser.getUsername();
        String newDemoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/demoteUserButton/" + promotedUser.getUsername();

        return new PromoteUserResponseDto(msg, newPromoteButtonUrl, newDemoteButtonUrl);
    }

    /**
     * Get a DemoteUserResponseDto for the given user which contains information about a successful demotion.
     *
     * @param demotedUser the promoted user
     * @return the DemoteUserResponseDto for the given user which contains information about a successful demotion
     */
    @Override
    public DemoteUserResponseDto getDemoteUserResponseDtoForUser(User demotedUser) {
        String msg = messageService.getMessage("User.authority.demotion.notification", demotedUser.getUsername(),
                demotedUser.getHighestAuthority().getDisplayRank());
        String newPromoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/promoteUserButton/" + demotedUser.getUsername();
        String newDemoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/demoteUserButton/" + demotedUser.getUsername();

        return new DemoteUserResponseDto(msg, newPromoteButtonUrl, newDemoteButtonUrl);
    }

    /**
     * Get the logged in user if they are not disciplined, or null if they are.
     *
     * @return the logged in user if they are not disciplined, or null if they are.
     */
    @Override
    public User getLoggedInUserIfNotDisciplined() {
        User loggedInUser = getLoggedInUser();
        if (loggedInUser == null || loggedInUser.isBannedOrSuspended()) {
            return null;
        }

        return loggedInUser;
    }

    /**
     * Helper method that adds a given userRole to a given user and then saves that user.
     *
     * @param user the user to add the role to.
     * @param userRole the role to add to the user
     */
    private void addAuthorityToUser(User user, UserRole userRole) {
        if (!user.hasAuthority(userRole)) {
            user.addAuthority(userRole);
            userRepository.save(user);
        }
    }

    /**
     * Helper method that parses search text.
     *
     * @param searchText the text to be parsed.
     * @return the list of tokens
     * @throws UnsupportedEncodingException
     */
    private List<String> parseSearchText(String searchText) throws UnsupportedEncodingException {
        return SearchParserHelper.parseSearchText(searchText);
    }

    /**
     * Helper method that converts a list of Users to a list of UserSearchResultDtos
     *
     * @param users the list of users to convert
     * @return a List of UserSearchResultDtos representing the given Users
     */
    private List<UserSearchResultDto> convertUserListToUserSearchResultDto(List<User> users) {
        List<UserSearchResultDto> results = new ArrayList<>();

        for (User user : users) {
            UserSearchResultDto dto = new UserSearchResultDto();
            dto.setUsername(user.getUsername());
            dto.setLastActive(timeCalculatorService.getTimeSinceUserLastActiveMessage(user));
            results.add(dto);
        }

        return results;
    }
}
