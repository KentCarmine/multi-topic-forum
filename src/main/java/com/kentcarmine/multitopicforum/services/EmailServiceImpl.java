package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Properties;

/**
 * Service implementation that provides actions related to sending emails.
 */
@Service
public class EmailServiceImpl implements EmailService {
    private static final String TRANSPORT_PROTOCOL_CODE = "mail.transport.protocol";
    private static final String SMTP_AUTH_CODE = "mail.smtp.auth";
    private static final String ENABLE_STARTTLS_CODE = "mail.smtp.starttls.enable";
    private static final String DEBUG_CODE = "mail.debug";

    private JavaMailSender mailSender;
    private UserAccountService userAccountService;
    private MessageService messageService;

    @Autowired
    public EmailServiceImpl(@Value("${spring.mail.host}") String mailHost,
                            @Value("${spring.mail.port}") int mailPort,
                            @Value("${spring.mail.username}") String mailUserName,
                            @Value("${spring.mail.password}") String mailPassword,
                            @Value("${mail.transport.protocol}") String protocol,
                            @Value("${mail.smtp.auth}") String smtpAuth,
                            @Value("${mail.smtp.starttls.enable}") String enableStartTls,
                            @Value("${mail.debug}") String debug,
                            UserAccountService userAccountService, MessageService messageService) {
        mailSender = initMailSender(mailHost, mailPort, mailUserName, mailPassword, protocol, smtpAuth, enableStartTls, debug);
        this.userAccountService = userAccountService;
        this.messageService = messageService;
    }

    private JavaMailSender initMailSender(String mailHost, int mailPort, String mailUserName, String mailPassword,
                                          String protocol, String smtpAuth, String enableStartTls, String debug) {
        JavaMailSenderImpl ms = new JavaMailSenderImpl();

        ms.setHost(mailHost);
        ms.setPort(mailPort);

        ms.setUsername(mailUserName);
        ms.setPassword(mailPassword);

        Properties props = ms.getJavaMailProperties();
        props.put(TRANSPORT_PROTOCOL_CODE, protocol);
        props.put(SMTP_AUTH_CODE, smtpAuth);
        props.put(ENABLE_STARTTLS_CODE, enableStartTls);
        props.put(DEBUG_CODE, debug);

        return ms;
    }

    /**
     * Helper method that sends an email to the user including a link to allow them to reset their password.
     *
     * @param appUrl the url to click on
     * @param locale the locale
     * @param token the token to send
     * @param user the user to whom the token belongs
     */
    @Override
    public void sendPasswordResetEmail(String appUrl, Locale locale, PasswordResetToken token, User user) {
        String resetUrl = appUrl + "/changePassword?username=" + user.getUsername() + "&token=" + token.getToken();
        String message = userAccountService.getPasswordResetEmailContent(resetUrl, locale);
        String subject = messageService.getMessage("User.passwordReset.confirmationEmail.subject");

        sendEmail(user.getEmail(), subject, message);
    }

    /**
     * Helper method that sends an email to the user including a link with an updated registration verification token.
     *
     * @param appUrl the url to click on
     * @param locale the locale
     * @param newToken the token to send
     * @param user the user attempting to register
     */
    @Override
    public void sendResendVerificationTokenEmail(String appUrl, Locale locale, VerificationToken newToken, User user) {
        String confirmationUrl = appUrl + "/registrationConfirm?token=" + newToken.getToken();
        String message = userAccountService.getResendVerificationTokenEmailContent(confirmationUrl, locale);
        String subject = messageService.getMessage("User.registration.confirmationEmail.resend.subject");

        sendEmail(user.getEmail(), subject, message);
    }

    @Async
    @Override
    public void sendEmail(String recipientEmail, String subject, String content) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientEmail);
        email.setSubject(subject);
        email.setText(content);

        try {
            mailSender.send(email);
        } catch (Exception e) {
            System.out.println("### mailSender.send() error");
            e.printStackTrace();
            throw e;
        }
    }
}
