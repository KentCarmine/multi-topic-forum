package com.kentcarmine.multitopicforum.listeners;

import com.kentcarmine.multitopicforum.events.OnRegistrationCompleteEvent;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.EmailService;
import com.kentcarmine.multitopicforum.services.MessageService;
import com.kentcarmine.multitopicforum.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listener that processes OnRegistrationCompleteEvents. Creates a verification token and emails a link to the user
 * so that they can complete their registration
 */
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final UserAccountService userAccountService;

    private final MessageService messageService;

    private final EmailService emailService;

    @Autowired
    public RegistrationListener(UserAccountService userAccountService, MessageService messageService, EmailService emailService) {
        this.userAccountService = userAccountService;
        this.emailService = emailService;
        this.messageService = messageService;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        this.confirmRegistration(onRegistrationCompleteEvent);
    }

    /**
     * Sends an email to the user containing information from the received event
     *
     * @param event the event to get information from
     */
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userAccountService.createVerificationToken(user, token);

        String recipientEmail = user.getEmail();
        String subject = messageService.getMessage("User.registration.confirmationEmail.subject");
        String confirmationUrl = event.getAppUrl() + "/registrationConfirm?token=" + token;
        String message = messageService.getMessage("message.regSucc", event.getLocale());
        String fullContent = message + "\n" + confirmationUrl;

        emailService.sendEmail(recipientEmail, subject, fullContent);
    }

}
