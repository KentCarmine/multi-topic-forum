package com.kentcarmine.multitopicforum.handlers;

import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handler that allows a user who successfully logs in to automatically redirect to their home page, or their
 * disciplinary status page if they are currently banned or suspended.
 */
@Component
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private UserService userService;

    public LoginAuthenticationSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        String loggedInUserName = auth.getName();
        User user = userService.getUser(loggedInUserName);

        if (user != null && user.isBannedOrSuspended()) {
            System.out.println("### in onAuthenticationSuccess banned case. User = " + user);
            String url = "/showDisciplineInfo/" + loggedInUserName;
            redirectStrategy.sendRedirect(req, res, url);
        } else {
            String url = "/users/" + loggedInUserName;
            redirectStrategy.sendRedirect(req, res, url);
        }
    }
}
