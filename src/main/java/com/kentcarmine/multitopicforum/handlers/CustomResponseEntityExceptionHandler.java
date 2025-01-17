package com.kentcarmine.multitopicforum.handlers;

import com.kentcarmine.multitopicforum.exceptions.*;
import com.kentcarmine.multitopicforum.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * A Handler that handles routing as a result of application errors.
 */
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    
    private MessageService messageService;

    @Autowired
    public CustomResponseEntityExceptionHandler(MessageService messageService) {
        super();
        this.messageService = messageService;
    }

    /**
     * Handler method that handles displaying an error page when a UserNotFoundException occurs.
     *
     * @param model the model to add a message to
     * @param ex the exception to handle
     * @return the name of the error page to display
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(Model model, UserNotFoundException ex) {
//        System.out.println("### in handleUserNotFound(). ex.message = " + ex.getMessage());
        String msg = messageService.getMessage(ex.getMessage(), ex.getUsername());
        System.out.println("### in handleUserNotFound, msg = " + msg);
        model.addAttribute("message", msg);
        return "user-not-found";
    }

    /**
     * Exception handler that shows an error page when a forum with a given name is not found.
     */
    @ExceptionHandler(ForumNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleForumNotFound(Model model, ForumNotFoundException ex) {
        String msg = messageService.getMessage(ex.getMessage());
        model.addAttribute("message", msg);
        return "forum-not-found";
    }

    /**
     * Exception handler that shows an error page when a forum with a given name is not found.
     */
    @ExceptionHandler(TopicThreadNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleThreadNotFound(Model model, TopicThreadNotFoundException ex) {
        String msg = messageService.getMessage(ex.getMessage());
        model.addAttribute("message", msg);
        return "thread-not-found";
    }

    @ExceptionHandler(PageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePageNotFound(Model model, PageNotFoundException ex) {
        String msg = messageService.getMessage(ex.getMessage());
        model.addAttribute("message", msg);
        return "general-error-page";
    }

    @ExceptionHandler({InsufficientAuthorityException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleInsufficientAuthority(InsufficientAuthorityException e) {
        logger.error(e);
        System.out.println("### in handleInsufficientAuthority");

//        return "redirect:/forbidden";
        return "access-denied-page";
    }

    @ExceptionHandler({DisciplineNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleDisciplineNotFound(DisciplineNotFoundException e, Model model) {
        logger.error(e);

        String msg = messageService.getMessage(e.getMessage());
        model.addAttribute("message", msg);
        return "general-error-page";
    }

    /**
     * Handle exception resulting from a disciplined user attempting to take actions that require authentication. Redirect
     * them to a page displaying their discipline state.
     *
     * @param e the triggering exception
     */
    @ExceptionHandler({DisciplinedUserException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleDisciplinedUserTakingActionRequiringAuth(DisciplinedUserException e) {
        logger.error(e);

        System.out.println("### in handleDisciplinedUserTakingActionRequiringAuth. User = " + e.getUser());

        return "redirect:/showDisciplineInfo/" + e.getUser().getUsername();
    }

    /**
     * Handle errors resulting from misconfigured email sender during user registration. Shows the user an error page.
     *
     * @param e the exception
     * @param request the web request
     * @return a ModelAndView representing the error page and a 500 Internal Server Error
     */
    @ExceptionHandler({MailAuthenticationException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleMailError(MailAuthenticationException e, HttpServletRequest request, Model model) {
        logger.error("500 status code", e);

        model.addAttribute("message", messageService.getMessage("message.email.config.error", request.getLocale()));
        return "registration-confirmation-error";
    }

    /**
     * Handle otherwise unspecified errors.
     *
     * @param e the exception
     * @param request the web request
     * @param model the model
     * @return a String representing the error page and a 500 Internal Server Error
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception e, HttpServletRequest request, Model model) {
        logger.error("500 status code", e);

        model.addAttribute("message", messageService.getMessage("message.unknownError", request.getLocale()));
        return "general-error-page";
    }
}
