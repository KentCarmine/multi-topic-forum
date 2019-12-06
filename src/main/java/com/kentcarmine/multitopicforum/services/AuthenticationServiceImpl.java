package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that provies information about the currently authenticated user.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private AuthenticationFacade authenticationFacade;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public String getLoggedInUserName() {
        if (isUserLoggedIn()) {
            return authenticationFacade.getAuthentication().getName();
        } else {
            return null;
        }
    }

    @Override
    public boolean isUserLoggedIn() {
        if (authenticationFacade.getAuthentication() == null) {
            return false;
        }

        return true;
    }

    @Override
    public void debugPrintAuthorities() {
        System.out.println("### Authorities");
        authenticationFacade.getAuthentication().getAuthorities().forEach((a) -> a.getAuthority());
        System.out.println("### End Authorities");
    }
}
