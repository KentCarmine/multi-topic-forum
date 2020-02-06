package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacadeImpl;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.repositories.AuthorityRepository;
import com.kentcarmine.multitopicforum.repositories.PasswordResetTokenRepository;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import com.kentcarmine.multitopicforum.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDtoToUserConverter userDtoToUserConverter;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthorityRepository authorityRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService,
                           UserDtoToUserConverter userDtoToUserConverter, PasswordEncoder passwordEncoder,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.userDtoToUserConverter = userDtoToUserConverter;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authorityRepository = authorityRepository;
    }

    /**
     * Gets the currently logged in user
     *
     * @return the currently logged in user. Can be ANONYMOUS
     */
    @Override
    public User getLoggedInUser() {
        return getUser(authenticationService.getLoggedInUserName());
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
     * Creates and saves a new User object from a given UserDto
     *
     * @param userDto the UserDto to convert and save
     * @return the created User object
     * @throws DuplicateEmailException if a user already exists with the given email
     * @throws DuplicateUsernameException if a user already exists with the given username
     */
    @Override
    public User createUserByUserDto(UserDto userDto) throws DuplicateEmailException, DuplicateUsernameException {
        return createUser(userDtoToUserConverter.convert(userDto));
    }

    /**
     * Saves the given user to persistent storage.
     *
     * @param user the user to save
     * @return the created User object
     * @throws DuplicateEmailException if a user already exists with the given email
     * @throws DuplicateUsernameException if a user already exists with the given username
     */
    @Transactional
    @Override
    public User createUser(User user) throws DuplicateEmailException, DuplicateUsernameException {
        if (usernameExists(user.getUsername())) {
            throw new DuplicateUsernameException("The username "+ user.getUsername() + " is taken.");
        }

        if (emailExists(user.getEmail())) {
            throw new DuplicateEmailException("Account with email address " + user.getEmail() + " already exists.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.addAuthority(UserRole.USER);

        return userRepository.save(user);
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

    /**
     * Get the user with the given verification token string
     *
     * @param verificationToken the verification token string to get the User for
     * @return the user with the given verification token, or null if no such user exists
     */
    @Override
    public User getUserByVerificationToken(String verificationToken) {
        User user = verificationTokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

    /**
     * Get the VerificationToken from persistent storage that has the given token string
     *
     * @param token the token string to get the VerificationToken for
     * @return the VerificationToken with the given token string, or null if no such VerificationToken exists
     */
    @Override
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    /**
     * Saves a registered user to persistent storage
     * @param user the user to save
     */
    @Override
    public void saveRegisteredUser(User user) {
        userRepository.save(user);
    }

    /**
     * Creates and saves a VerificationToken with the given token string that is associated with the given User
     *
     * @param user the user to associate the token with
     * @param token the token string to use
     */
    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepository.save(myToken);
    }

    /**
     * Updates an existing VerificationToken with the given existingToken string to use a new token string, and saves
     * that VerificationToken
     * @param existingToken the existing token string to find the VerificationToken for
     * @return the updated VerificationToken
     */
    @Override
    public VerificationToken generateNewVerificationToken(String existingToken) {
        VerificationToken token = verificationTokenRepository.findByToken(existingToken);
        token.updateToken(UUID.randomUUID().toString());
        token = verificationTokenRepository.save(token);
        return token;
    }

    /**
     * Creates and saves a PasswordResetToken associated with the given User. Also deletes all other PasswordResetTokens
     * associated with that user
     *
     * @param user the user to associate the PasswordResetToken with
     * @return a PasswordResetToken associated with the given User
     */
    @Transactional
    @Override
    public PasswordResetToken createPasswordResetTokenForUser(User user) {
        passwordResetTokenRepository.deleteByUser(user);
        authorityRepository.deleteByUserAndAuthority(user, UserRole.CHANGE_PASSWORD_PRIVILEGE);
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken prToken = new PasswordResetToken(tokenStr, user);
        prToken = passwordResetTokenRepository.save(prToken);

        return prToken;
    }

    /**
     * Confirms that the given User is associated with a PasswordResetToken with the given token string. Also updates
     * the given User's authority, allowing them to change their password.
     *
     * @param user the user to validate
     * @param token the token string to find a PasswordResetToken for
     * @return true if the User has the authority to reset their password, false otherwise
     */
    @Override
    public boolean validatePasswordResetToken(User user, String token) {
        if (user == null || !user.isEnabled()) {
            return false;
        }

        // Invalid username or token
        PasswordResetToken prToken = passwordResetTokenRepository.findByToken(token);
        if (prToken == null || !prToken.getUser().getUsername().equals(user.getUsername())) {
            return false;
        }

        // Expired token
        Calendar calendar = Calendar.getInstance();
        if (prToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0) {
            return false;
        }

        // Valid username and token
        addAuthorityToUser(user, UserRole.CHANGE_PASSWORD_PRIVILEGE);

        return true;
    }

    /**
     * Updates the given user's password to newPassword and revokes the now-unneeded password change authority.
     *
     * @param user the user to update
     * @param newPassword the new password for that user
     */
    @Transactional
    @Override
    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.removeAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE);
        authorityRepository.deleteByUserAndAuthority(user, UserRole.CHANGE_PASSWORD_PRIVILEGE);
        passwordResetTokenRepository.deleteByUser(user);
        userRepository.save(user);
    }

    /**
     * Searches for the names of all Users that have names that contain all tokens (delimited on double quotes and
     * spaces, but not spaces within double quotes) of the given search text.
     *
     * @param searchText The text to search for
     * @return the set of usernames of Users (ordered alphabetically) that match the search terms
     * @throws UnsupportedEncodingException
     */
    public SortedSet<String> searchForUsernames(String searchText) throws UnsupportedEncodingException {
        SortedSet<User> users = searchForUsers(searchText);
        SortedSet<String> usernames = new TreeSet<>(users.stream().map(User::getUsername).collect(Collectors.toList()));
        return usernames;
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
    public SortedSet<User> searchForUsers(String searchText) throws UnsupportedEncodingException {
        SortedSet<User> users = new TreeSet<>((o1, o2) -> o1.getUsername().toLowerCase().compareTo(o2.getUsername().toLowerCase()));

        List<String> searchTerms = parseSearchText(searchText);
        List<List<User>> searchTermResults = new ArrayList<>();
        for (int i = 0; i < searchTerms.size(); i++) {
            searchTermResults.add(new ArrayList<User>());
        }

        for(int i = 0; i < searchTerms.size(); i++) {
            String st = searchTerms.get(i);
            searchTermResults.set(i, userRepository.findByUsernameLikeIgnoreCase("%" + st + "%"));
        }

        if (!searchTermResults.isEmpty()) {
            users.addAll(searchTermResults.get(0));
            searchTermResults.remove(0);
            for (List<User> str : searchTermResults) {
                users.retainAll(str);
            }
        }

        return users;
    }

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
}
