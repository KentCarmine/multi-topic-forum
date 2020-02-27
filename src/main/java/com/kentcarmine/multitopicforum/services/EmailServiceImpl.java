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

    private JavaMailSender mailSender;
    private UserService userService;

    @Autowired
    public EmailServiceImpl(@Value("${spring.mail.host}") String mailHost, @Value("${spring.mail.port}") int mailPort,
                            @Value("${spring.mail.username}") String mailUserName,
                            @Value("${spring.mail.password}") String mailPassword, UserService userService) {
        mailSender = initMailSender(mailHost, mailPort, mailUserName, mailPassword);
        this.userService = userService;
    }

    private JavaMailSender initMailSender(String mailHost, int mailPort, String mailUserName, String mailPassword) {
        JavaMailSenderImpl ms = new JavaMailSenderImpl();

        ms.setHost(mailHost);
        ms.setPort(mailPort);

        ms.setUsername(mailUserName);
        ms.setPassword(mailPassword);

        // TODO: Move into properties file?
        Properties props = ms.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

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
        String message = userService.getPasswordResetEmailContent(resetUrl, locale);
        String subject = "Multi-Topic Forum Password Reset";

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
        String message = userService.getResendVerificationTokenEmailContent(confirmationUrl, locale);
        String subject = "Multi-Topic Forum Registration Confirmation : Re-sent";

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
