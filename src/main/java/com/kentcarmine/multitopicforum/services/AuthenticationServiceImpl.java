package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private AuthenticationFacade authenticationFacade;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
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

    @Override
    public boolean isUserLoggedIn() {
        if (authenticationFacade.getAuthentication() == null) {
            return false;
        }

        return true;
    }
}
