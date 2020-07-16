package com.kentcarmine.multitopicforum.helpers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Helper to provide non-static access to logged in user auth information.
 */
@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserDetails getPrincipal() {
        Authentication auth = getAuthentication();
        if (auth == null) {
            return null;
        }

        return (UserDetails)getAuthentication().getPrincipal();
    }
}
