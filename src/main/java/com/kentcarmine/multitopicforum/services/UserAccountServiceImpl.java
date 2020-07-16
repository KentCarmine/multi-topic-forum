package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import com.kentcarmine.multitopicforum.repositories.AuthorityRepository;
import com.kentcarmine.multitopicforum.repositories.PasswordResetTokenRepository;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import com.kentcarmine.multitopicforum.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository userRepository;
    private final UserDtoToUserConverter userDtoToUserConverter;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthorityRepository authorityRepository;
    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public UserAccountServiceImpl(UserRepository userRepository, UserDtoToUserConverter userDtoToUserConverter,
                                  PasswordEncoder passwordEncoder,
                                  VerificationTokenRepository verificationTokenRepository,
                                  PasswordResetTokenRepository passwordResetTokenRepository,
                                  AuthorityRepository authorityRepository, MessageService messageService,
                                  UserService userService) {
        this.userRepository = userRepository;
        this.userDtoToUserConverter = userDtoToUserConverter;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authorityRepository = authorityRepository;
        this.messageService = messageService;
        this.userService = userService;
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
        if (userService.usernameExists(user.getUsername())) {
            throw new DuplicateUsernameException();
        }

        if (userService.emailExists(user.getEmail())) {
            throw new DuplicateEmailException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.addAuthority(UserRole.USER);

        return userRepository.save(user);
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

//        System.out.println("### in UserAccountServiceImpl.isVerificationTokenExpired");
//        System.out.println("### verificationToken.getExpiryDate().getTime() = " + verificationToken.getExpiryDate().getTime());
//        System.out.println("### calendar.getTime().getTime() = " + calendar.getTime().getTime());
//        System.out.println("### verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime() = " + (verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime()));
//        System.out.println("### user != null = " + user != null);
//        System.out.println("### !user.isEnabled() = " + !user.isEnabled());
//        System.out.println("### returning = " + (verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0 && user != null && !user.isEnabled()));
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
     * Returns a message in the given locale that indicates that the authentication token is invalid.
     *
     * @param locale the locale of the message to get
     * @return a message in the given locale that indicates that the authentication token is invalid
     */
    @Override
    public String getInvalidAuthTokenMessage(Locale locale) {
        return messageService.getMessage("auth.message.invalidToken", locale);
    }

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


}
