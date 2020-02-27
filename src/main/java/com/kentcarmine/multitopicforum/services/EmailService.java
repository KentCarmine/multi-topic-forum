package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.PasswordResetToken;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.VerificationToken;

import java.util.Locale;

/**
 * Specification for services that provide actions related to sending emails.
 */
public interface EmailService {
    void sendEmail(String recipientEmail, String subject, String content);

    void sendPasswordResetEmail(String appUrl, Locale locale, PasswordResetToken token, User user);

    void sendResendVerificationTokenEmail(String appUrl, Locale locale, VerificationToken newToken, User user);
}
