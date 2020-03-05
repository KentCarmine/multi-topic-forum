package com.kentcarmine.multitopicforum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service that handles translating message codes from properties to their corresponding strings.
 */
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageSource messageSource;

    @Autowired
    public MessageServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String id, Locale locale) {
        return messageSource.getMessage(id, null, locale);
    }

    @Override
    public String getMessage(String id) {
        return getMessage(id, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String id, Locale locale, Object... args) {
        return messageSource.getMessage(id, args, locale);
    }

    @Override
    public String getMessage(String id, Object... args) {
        return getMessage(id, LocaleContextHolder.getLocale(), args);
    }
}
