package com.kentcarmine.multitopicforum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageSource messageSource;

    @Autowired
    public MessageServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String id, Locale locale) {
//        System.out.println("### In MessageService case 1");
        return messageSource.getMessage(id, null, locale);
    }

    @Override
    public String getMessage(String id) {
//        System.out.println("### In MessageService case 2");
//        System.out.println("### Locale = " + LocaleContextHolder.getLocale());
        return getMessage(id, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String id, Locale locale, Object... args) {
//        System.out.println("### In MessageService case 3");
        return messageSource.getMessage(id, args, locale);
    }

    @Override
    public String getMessage(String id, Object... args) {
//        System.out.println("### In MessageService case 4");
//        System.out.println("### Locale = " + LocaleContextHolder.getLocale());
        return getMessage(id, LocaleContextHolder.getLocale(), args);
    }
}
