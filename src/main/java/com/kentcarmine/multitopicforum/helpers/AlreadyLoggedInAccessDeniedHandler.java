package com.kentcarmine.multitopicforum.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AlreadyLoggedInAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException, ServletException {
        String loggedInUsername = req.getUserPrincipal().getName();
        String userHome = "/users/" + loggedInUsername;

        System.out.println("### START ###");
        System.out.println("### REQUEST ###");
        System.out.println(req.toString());
        System.out.println("### RESPONSE ###");
        System.out.println(res.toString());
        System.out.println("### EXCEPTION ###");
        System.out.println(ex.toString());
        System.out.println("Logged In User: " + loggedInUsername);
        System.out.println("### END ###");
//        res.setStatus(HttpServletResponse.SC_FOUND);

        res.sendRedirect(userHome);
//        req.getRequestDispatcher("/login").forward(req, res);
    }
}
