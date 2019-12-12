package com.kentcarmine.multitopicforum.handlers;

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

    /**
     * Handle errors resulting from misconfigured email sender during user registration. Shows the user an error page.
     *
     * @param e the exception
     * @param request the web request
     * @return a ModelAndView representing the error page and a 500 Internal Server Error
     */
    @ExceptionHandler({MailAuthenticationException.class})
    public ModelAndView handleMailError(RuntimeException e, HttpServletRequest request) {
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
    public ModelAndView handleGenericError(RuntimeException e, HttpServletRequest request) {
        logger.error("500 status code", e);

        ModelAndView mv = new ModelAndView("general-error-page", HttpStatus.INTERNAL_SERVER_ERROR);
        mv.getModel().put("message", messageSource.getMessage("message.unknownError", null, request.getLocale()));
        return mv;
    }
}