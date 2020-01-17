package com.kentcarmine.multitopicforum.services;

/**
 * Specification for services that provide actions related to sending emails.
 */
public interface EmailService {
    void sendEmail(String recipientEmail, String subject, String content);
}
