package com.kentcarmine.multitopicforum.handlers;

import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.exceptions.InsufficientAuthorityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * A Handler that handles routing as a result of application errors.
 */
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private MessageSource messageSource;

    @Autowired
    public CustomResponseEntityExceptionHandler(MessageSource messageSource) {
        super();
        this.messageSource = messageSource;
    }

    @ExceptionHandler({InsufficientAuthorityException.class})
    public ModelAndView handleInsuffcientAuthority(InsufficientAuthorityException e) {
        logger.error(e);

        ModelAndView mv = new ModelAndView("redirect:/forbidden");
        mv.setStatus(HttpStatus.UNAUTHORIZED);
        return mv;
    }

    /**
     * Handle exception resulting from a disciplined user attempting to take actions that require authentication. Redirect
     * them to a page displaying their discipline state.
     *
     * @param e the triggering exception
     */
    @ExceptionHandler({DisciplinedUserException.class})
    public ModelAndView handleDisciplinedUserTakingActionRequiringAuth(DisciplinedUserException e) {
        logger.error(e);

        System.out.println("### in handleDisciplinedUserTakingActionRequiringAuth. User = " + e.getUser());

        ModelAndView mv = new ModelAndView("redirect:/showDisciplineInfo/" + e.getUser().getUsername());
        return mv;
    }

    /**
     * Handle errors resulting from misconfigured email sender during user registration. Shows the user an error page.
     *
     * @param e the exception
     * @param request the web request
     * @return a ModelAndView representing the error page and a 500 Internal Server Error
     */
    @ExceptionHandler({MailAuthenticationException.class})
    public ModelAndView handleMailError(MailAuthenticationException e, HttpServletRequest request) {
        logger.error("500 status code", e);

        ModelAndView mv = new ModelAndView("registration-confirmation-error", HttpStatus.INTERNAL_SERVER_ERROR);
        mv.getModel().put("message", messageSource.getMessage("message.email.config.error", null, request.getLocale()));
        return mv;
    }

    /**
     * Handle otherwise unspecified errors.
     *
     * @param e the exception
     * @param request the web request
     * @return a ModelAndView representing the error page and a 500 Internal Server Error
     */
    @ExceptionHandler({Exception.class})
    public ModelAndView handleGenericError(Exception e, HttpServletRequest request) {
        logger.error("500 status code", e);

        ModelAndView mv = new ModelAndView("general-error-page", HttpStatus.INTERNAL_SERVER_ERROR);
        mv.getModel().put("message", messageSource.getMessage("message.unknownError", null, request.getLocale()));
        return mv;
    }
}
