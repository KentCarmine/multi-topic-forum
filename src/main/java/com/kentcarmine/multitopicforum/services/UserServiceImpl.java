package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
    }

    public String getLoggedInUserName() {
        return authenticationFacade.getAuthentication().getName();
    }

    public User getLoggedInUser() {
        return getUser(getLoggedInUserName());
    }

    public User getUser(String name) {
        Optional<User> userOpt = userRepository.findById(name);

        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            return null;
        }
    }

    public boolean userWithNameExists(String name) {
        Optional<User> userOpt = userRepository.findById(name);

        return userOpt.isPresent();
    }

//    public void printAuth() {
//        authenticationFacade.printAuthorities();
//    }
}
