package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.dtos.UserDto;
import com.kentcarmine.multitopicforum.exceptions.DuplicateEmailException;
import com.kentcarmine.multitopicforum.exceptions.DuplicateUsernameException;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;
    private final UserDtoToUserConverter userDtoToUserConverter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthenticationFacade authenticationFacade, UserDtoToUserConverter userDtoToUserConverter, PasswordEncoder passwordEncoder) {
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
        this.userDtoToUserConverter = userDtoToUserConverter;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String getLoggedInUserName() {
//        System.out.println("#####");
//        System.out.println(authenticationFacade);
//        System.out.println(authenticationFacade.getAuthentication());
//        System.out.println(authenticationFacade.getAuthentication().getName());
//        System.out.println(authenticationFacade.getAuthentication().getName());
//        System.out.println("#####");
        if (isUserLoggedIn()) {
            return authenticationFacade.getAuthentication().getName();
        } else {
            return null;
        }
    }

    public boolean isUserLoggedIn() {
        if (authenticationFacade.getAuthentication() == null) {
            return false;
        }

        return true;
    }

    @Override
    public User getLoggedInUser() {
        return getUser(getLoggedInUserName());
    }

    @Override
    public User getUser(String name) {
        if (name == null) {
            return null;
        }

        Optional<User> userOpt = userRepository.findById(name);

        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            return null;
        }
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
        return userRepository.findById(username).isPresent();
    }



    //    public void printAuth() {
//        authenticationFacade.printAuthorities();
//    }
}
