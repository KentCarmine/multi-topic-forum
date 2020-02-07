package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.helpers.AuthenticationFacade;
import com.kentcarmine.multitopicforum.model.Authority;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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

    /**
     * Ensure that the authorities of the given logged in user are up-to-date and consistent with those stored in the
     * database
     *
     * @param loggedInUser the logged in user
     */
    @Override
    public void updateAuthorities(User loggedInUser) {
        if (loggedInUser == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Set<GrantedAuthority> updatedAuthorities = new HashSet<>(authentication.getAuthorities());
        for(Authority role : loggedInUser.getAuthorities()) {
            updatedAuthorities.add(new SimpleGrantedAuthority(role.getAuthority().toString()));
        }

        Authentication newAuth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), updatedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @Override
    public void debugPrintAuthorities() {
        System.out.println("### Authorities");
        authenticationFacade.getAuthentication().getAuthorities().forEach((a) -> a.getAuthority());
        System.out.println("### End Authorities");
    }
}
