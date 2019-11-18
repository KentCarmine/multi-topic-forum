package com.kentcarmine.multitopicforum.helpers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Helper to provide non-static access to logged in user auth information.
 */
@Component
public interface AuthenticationFacade {
    Authentication getAuthentication();

    UserDetails getPrincipal();

//    void printAuthorities();
}
