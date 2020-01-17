package com.kentcarmine.multitopicforum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Service implementation that provides actions related to sending emails.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(@Value("${spring.mail.host}") String mailHost, @Value("${spring.mail.port}") int mailPort,
                            @Value("${spring.mail.username}") String mailUserName,
                            @Value("${spring.mail.password}") String mailPassword) {
        mailSender = initMailSender(mailHost, mailPort, mailUserName, mailPassword);
    }

    private JavaMailSender initMailSender(String mailHost, int mailPort, String mailUserName, String mailPassword) {
        JavaMailSenderImpl ms = new JavaMailSenderImpl();

        ms.setHost(mailHost);
        ms.setPort(mailPort);

        ms.setUsername(mailUserName);
        ms.setPassword(mailPassword);

        Properties props = ms.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return ms;
    }

//    @Async
    @Override
    public void sendEmail(String recipientEmail, String subject, String content) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientEmail);
        email.setSubject(subject);
        email.setText(content);
        mailSender.send(email);
    }
}
