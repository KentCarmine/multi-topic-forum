package com.kentcarmine.multitopicforum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Class that handles general-purpose configurations.
 */
@Configuration
public class GeneralConfig {

    @Bean
    public LocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }
}
