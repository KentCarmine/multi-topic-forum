package com.kentcarmine.multitopicforum.handlers;

import com.kentcarmine.multitopicforum.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handler class that displays errors when a failing to authenticate.
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final UserAccountService userAccountService;

    @Autowired
    public CustomAuthenticationFailureHandler(UserAccountService userAccountService) {
        super();
        this.userAccountService = userAccountService;
    }

    public CustomAuthenticationFailureHandler(String defaultFailureUrl, UserAccountService userAccountService) {
        super(defaultFailureUrl);
        this.userAccountService = userAccountService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        setDefaultFailureUrl("/login?regError");

        super.onAuthenticationFailure(request, response, exception);

        String errorMessage = userAccountService.getAuthenticationFailureMessage(exception, request.getLocale());

        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorMessage);
    }
}
