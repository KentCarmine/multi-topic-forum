package com.kentcarmine.multitopicforum.services;

import java.util.Locale;

/**
 * Specification for services that provide actions related to messages
 */
public interface MessageService {
    String getMessage(String id, Locale locale);
}
