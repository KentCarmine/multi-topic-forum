package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.helpers.SearchParserHelper;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import javax.naming.AuthenticationException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
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
    private final DisciplineRepository disciplineRepository;
    private final DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter;
    private final UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;
    private final MessageService messageService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService,
                           UserDtoToUserConverter userDtoToUserConverter, PasswordEncoder passwordEncoder,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           AuthorityRepository authorityRepository, DisciplineRepository disciplineRepository,
                           DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter,
                           UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter,
                           MessageService messageService) {
        this.userRepository = userRepository;
        this.userDtoToUserConverter = userDtoToUserConverter;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authorityRepository = authorityRepository;
        this.disciplineRepository = disciplineRepository;
        this.disciplineToDisciplineViewDtoConverter = disciplineToDisciplineViewDtoConverter;
        this.userToUserRankAdjustmentDtoConverter = userToUserRankAdjustmentDtoConverter;
        this.messageService = messageService;
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

//        if (loggedInUser.isBannedOrSuspended()) {
//            forceLogOut(loggedInUser);
//        }

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
        user.setEnabled(true);
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
     * Checks if the given verification token is expired or invalid.
     *
     * @param verificationToken the token to check for expiry
     * @return true if the token is current and valid, false otherwise
     */
    @Override
    public boolean isVerificationTokenExpired(VerificationToken verificationToken) {
        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();

        return verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0
                && user != null && !user.isEnabled();
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
     * Creates a new discipline entry described by the UserDisciplineSubmissionDto and created by the loggedInUser.
     *
     * @param userDisciplineSubmissionDto describes the discipinary action taken and against which user
     * @param loggedInUser the logged in user
     */
    @Override
    @Transactional
    public boolean disciplineUser(UserDisciplineSubmissionDto userDisciplineSubmissionDto, User loggedInUser) {
        User disciplinedUser = getUser(userDisciplineSubmissionDto.getDisciplinedUsername());

        DisciplineType disciplineType = userDisciplineSubmissionDto.isBan() ? DisciplineType.BAN : DisciplineType.SUSPENSION;

        System.out.println("### in disciplineUser(). disciplinedUser.isBanned() = " + disciplinedUser.isBanned());
        System.out.println("### in disciplineUser(). disciplineType = " + disciplineType);

        if (disciplineType.equals(DisciplineType.BAN) && disciplinedUser.isBanned()) {
            return false;
        }

        Discipline discipline = new Discipline(disciplinedUser, loggedInUser, disciplineType, Date.from(Instant.now()),
                userDisciplineSubmissionDto.getReason());

        if (disciplineType.equals(DisciplineType.SUSPENSION)) {
            discipline.setDisciplineDurationHours(Integer.parseInt(userDisciplineSubmissionDto.getSuspensionHours()));
        }

        discipline = disciplineRepository.save(discipline);

        disciplinedUser.addDiscipline(discipline);

        disciplinedUser = userRepository.save(disciplinedUser);

        return true;
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
     * Throw a DisciplinedUserException if the given user has any active disciplines
     *
     * @param user the user to check for active disiciplines
     * @throws DisciplinedUserException if the given user has active disciplines
     */
    @Override
    public void handleDisciplinedUser(User user) throws DisciplinedUserException {
        if (user != null && user.isBannedOrSuspended()) {
            System.out.println("### in handleDisciplinedUser() fire exception case for " + user);
            throw new DisciplinedUserException(user);
        }
    }

    /**
     * Get a SortedSet of DisciplineViewDtos for all the given user's active disciplines.
     *
     * @param user the user to get active disciplines for
     * @param loggedInUser the logged in user
     * @return  SortedSet of DisciplineViewDtos for all the given user's active disciplines
     */
    @Override
    public SortedSet<DisciplineViewDto> getActiveDisciplinesForUser(User user, User loggedInUser) {
        Comparator<DisciplineViewDto> comparator = new Comparator<DisciplineViewDto>() {
            @Override
            public int compare(DisciplineViewDto o1, DisciplineViewDto o2) {
                if (o1.isBan() && o2.isBan()) {
                    return 0;
                } else if (o1.isBan()) {
                    return 1;
                } else if (o2.isBan()) {
                    return -1;
                }

                if (Integer.parseInt(o1.getDisciplineDuration()) > Integer.parseInt(o2.getDisciplineDuration())) {
                    return 1;
                } else if (Integer.parseInt(o2.getDisciplineDuration()) > Integer.parseInt(o1.getDisciplineDuration())) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        return getSortedDisciplineViewDtos(user.getActiveDisciplines(), comparator, loggedInUser);
    }

    /**
     * Get a SortedSet of DisciplineViewDtos for all the given user's inactive disciplines.
     *
     * @param user the user to get inactive disciplines for
     * @return  SortedSet of DisciplineViewDtos for all the given user's inactive disciplines
     */
    @Override
    public SortedSet<DisciplineViewDto> getInactiveDisciplinesForUser(User user) {
        Comparator<DisciplineViewDto> comparator = new Comparator<DisciplineViewDto>() {
            @Override
            public int compare(DisciplineViewDto o1, DisciplineViewDto o2) {
                if (o1.isBan() && o2.isBan()) {
                    return 0;
                } else if (o1.isBan()) {
                    return 1;
                } else if (o2.isBan()) {
                    return -1;
                } else {
                    return o1.getDisciplinedUntil().compareTo(o2.getDisciplinedUntil());
                }
            }
        };

        return getSortedDisciplineViewDtos(user.getInactiveDisciplines(), comparator, null);
    }

    /**
     * Helper method that converts a Set of Disciplines into a SortedSet of DisciplineViewDtos sorted by duration.
     *
     * @param disciplines the set of Disciplines to convert
     * @return a SortedSet of DisciplineViewDtos sorted by duration
     */
    private SortedSet<DisciplineViewDto> getSortedDisciplineViewDtos(Set<Discipline> disciplines, Comparator<DisciplineViewDto> comparator, User loggedInUser) {
        SortedSet<DisciplineViewDto> dtoSet = new TreeSet<>(comparator);

        for (Discipline d : disciplines) {
            DisciplineViewDto dto = disciplineToDisciplineViewDtoConverter.convert(d);

            dto.setCanRescind(loggedInUser != null && (loggedInUser.equals(d.getDiscipliningUser())
                    || loggedInUser.isHigherAuthority(d.getDiscipliningUser())));

            dtoSet.add(dto);
        }

        return dtoSet;
    }

    /**
     * Find the discipline object with the given ID and associated with the given user. Returns null if no such object
     * exists.
     *
     * @param id the id of the Discipline object to find
     * @param user the user being Disciplined by the object with the given id
     * @return the Discipline object, or null
     */
    @Override
    public Discipline getDisciplineByIdAndUser(Long id, User user) {
        Optional<Discipline> discOpt = disciplineRepository.findById(id);

        if (discOpt.isEmpty() || user == null) {
            return null;
        }

        Discipline discipline = discOpt.get();

        if (!discipline.getDisciplinedUser().equals(user)) {
            return null;
        }

        return discipline;
    }

    @Override
    @Transactional
    public void rescindDiscipline(Discipline disciplineToRescind) {
        disciplineToRescind.setRescinded(true);
        disciplineRepository.save(disciplineToRescind);
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

    // TODO: Refactor into UserAccountService
    /**
     * Returns a message in the given locale that indicates that the authentication token is invalid.
     *
     * @param locale the locale of the message to get
     * @return a message in the given locale that indicates that the authentication token is invalid
     */
    @Override
    public String getInvalidAuthTokenMessage(Locale locale) {
        return messageService.getMessage("auth.message.invalidToken", locale);
    }

    // TODO: Refactor into UserAccountService
    /**
     * Returns a message in the given locale that indicates that the authentication token is expired or invalid.
     *
     * @param locale the locale of the message to get
     * @return a message in the given locale that indicates that the authentication token is expired or invalid.
     */
    @Override
    public String getExpiredAuthTokenMessage(Locale locale) {
        return messageService.getMessage("auth.message.expired", locale);
    }

    // TODO: Refactor into DisciplineService
    /**
     * Returns a message that informs they user that they have been disciplined, the reason for this, and the
     * discipline's duration.
     *
     * @param greatestDurationActiveDiscipline the discipline with the greatest duration for the logged in user
     * @return a message that informs they user that they have been disciplined, the reason for this, and the
     * discipline's duration.
     */
    @Override
    public String getLoggedInUserBannedInformationMessage(Discipline greatestDurationActiveDiscipline) {
        StringBuilder msgBuilder = new StringBuilder("You have been ");

        if (greatestDurationActiveDiscipline.isBan()) {
            msgBuilder.append("permanently banned.");
        } else {
            String endsAtStr = greatestDurationActiveDiscipline.getDisciplineEndTime().toString();
            msgBuilder.append("suspended. Your suspension will end at: " + endsAtStr + ".");
        }

        msgBuilder.append(" The reason given for this disciplinary action was: " + greatestDurationActiveDiscipline.getReason());

        return msgBuilder.toString();
    }

    // TODO: Refactor into UserAccountService OR EmailService

    /**
     * Get the main content of the password reset email body in the given locale.
     *
     * @param resetUrl the reset password URL for the user to click.
     * @param locale the locale of the message
     * @return the main content of the password reset email body in the given locale.
     */
    @Override
    public String getPasswordResetEmailContent(String resetUrl, Locale locale) {

        return messageService.getMessage("message.resetPasswordLinkPrompt", locale) + "\n" + resetUrl;
    }

    // TODO: Refactor into UserAccountService OR EmailService
    /**
     * Get the main content of the resend verification token email in the given locale.
     *
     * @param confirmationUrl the confirmation URL for the user to click.
     * @param locale the locale of the message
     * @return the main content of the resend verification token email in the given locale.
     */
    @Override
    public String getResendVerificationTokenEmailContent(String confirmationUrl, Locale locale) {
        return messageService.getMessage("message.resendToken", locale) + "\n" + confirmationUrl;
    }

    // TODO: Refactor into UserAccountService

    /**
     * Get a message in the given locale to display to users indicating that authentication failed, and why it failed.
     *
     * @param authException the exception to get details about the failure from
     * @param locale the locale of the message to display.
     * @return a message in the given locale to display to users indicating that authentication failed, and why it
     * failed
     */
    @Override
    public String getAuthenticationFailureMessage(Exception authException, Locale locale) {
        String errorMessage = messageService.getMessage("message.badCredentials", locale);

        if (authException.getMessage().equalsIgnoreCase("User is disabled")) {
            errorMessage = messageService.getMessage("auth.message.disabled", locale);
        } else if (authException.getMessage().equalsIgnoreCase("User account has expired")) {
            errorMessage = messageService.getMessage("auth.message.expired", locale);
        }

        return errorMessage;
    }

    /**
     * Get a PromoteUserResponseDto for the given user which contains information about a successful promotion.
     *
     * @param promotedUser the promoted user
     * @return the PromoteUserResponseDto for the given user which contains information about a successful promotion
     */
    @Override
    public PromoteUserResponseDto getPromoteUserResponseDtoForUser(User promotedUser) {
        String msg = promotedUser.getUsername() + " promoted to "
                + promotedUser.getHighestAuthority().getDisplayRank() + ".";
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
        String msg = demotedUser.getUsername() + " demoted to "
                + demotedUser.getHighestAuthority().getDisplayRank() + ".";
        String newPromoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/promoteUserButton/" + demotedUser.getUsername();
        String newDemoteButtonUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString()
                + "/demoteUserButton/" + demotedUser.getUsername();

        return new DemoteUserResponseDto(msg, newPromoteButtonUrl, newDemoteButtonUrl);
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
