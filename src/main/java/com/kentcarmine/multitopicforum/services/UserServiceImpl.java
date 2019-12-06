package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacadeImpl;
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
import java.util.UUID;

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

    @Override
    public User getLoggedInUser() {
        return getUser(authenticationService.getLoggedInUserName());
    }

    @Override
    public User getUser(String name) {
        if (name == null) {
            return null;
        }

        return userRepository.findByUsername(name);
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email);
    }

    @Override
    public User createUserByUserDto(UserDto userDto) throws DuplicateEmailException, DuplicateUsernameException {
        return createUser(userDtoToUserConverter.convert(userDto));
    }

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

    @Override
    public boolean emailExists(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Override
    public User getUserByVerificationToken(String verificationToken) {
        User user = verificationTokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepository.save(myToken);
    }

    @Override
    public VerificationToken generateNewVerificationToken(String existingToken) {
        VerificationToken token = verificationTokenRepository.findByToken(existingToken);
        token.updateToken(UUID.randomUUID().toString());
        token = verificationTokenRepository.save(token);
        return token;
    }

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

    @Override
    public boolean validatePasswordResetToken(User user, String token) {
        if (user == null || !user.isEnabled()) {
//            System.out.println("### in validatePasswordResetToken invalid user case");
            return false;
        }

        // Invalid username or token
        PasswordResetToken prToken = passwordResetTokenRepository.findByToken(token);
        if (prToken == null || !prToken.getUser().getUsername().equals(user.getUsername())) {
//            System.out.println("### in validatePasswordResetToken invalid token case");
            return false;
        }

        // Expired token
        Calendar calendar = Calendar.getInstance();
        if (prToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0) {
            System.out.println("### in validatePasswordResetToken expired token case");
            return false;
        }

//        System.out.println("### in validatePasswordResetToken valid token case");

        // Valid username and token
        addAuthorityToUser(user, UserRole.CHANGE_PASSWORD_PRIVILEGE);

        return true;
    }

    @Transactional
    @Override
    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.removeAuthority(UserRole.CHANGE_PASSWORD_PRIVILEGE);
        authorityRepository.deleteByUserAndAuthority(user, UserRole.CHANGE_PASSWORD_PRIVILEGE);
        passwordResetTokenRepository.deleteByUser(user);
        userRepository.save(user);
    }

    private void addAuthorityToUser(User user, UserRole userRole) {
        if (!user.hasAuthority(userRole)) {
            user.addAuthority(userRole);
            userRepository.save(user);
        }
    }

}
