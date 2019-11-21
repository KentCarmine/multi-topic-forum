package com.kentcarmine.multitopicforum.helpers;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AlreadyLoggedInFailureHandler implements AuthenticationFailureHandler {

    public AlreadyLoggedInFailureHandler() {

    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        System.out.println("### START ###");
        System.out.println("### REQUEST ###");
        System.out.println(httpServletRequest.toString());
        System.out.println("### RESPONSE ###");
        System.out.println(httpServletResponse.toString());
        System.out.println("### EXCEPTION ###");
        System.out.println(e.toString());
        System.out.println("### END ###");
    }
}
