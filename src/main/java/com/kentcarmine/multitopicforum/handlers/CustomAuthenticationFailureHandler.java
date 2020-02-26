package com.kentcarmine.multitopicforum.handlers;

import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Handler class that displays errors when a failing to authenticate.
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final UserService userService;

    @Autowired
    public CustomAuthenticationFailureHandler(UserService userService) {
        super();
        this.userService = userService;
    }

    public CustomAuthenticationFailureHandler(String defaultFailureUrl, UserService userService) {
        super(defaultFailureUrl);
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        setDefaultFailureUrl("/login?regError");

        super.onAuthenticationFailure(request, response, exception);

        String errorMessage = userService.getAuthenticationFailureMessage(exception, request.getLocale());

        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorMessage);
    }
}
