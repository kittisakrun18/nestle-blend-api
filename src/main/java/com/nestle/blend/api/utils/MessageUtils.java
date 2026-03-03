package com.nestle.blend.api.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtils {

    @Autowired
    private MessageSource messageSource;

    public int getCode(String key) {
        String code = this.messageSource.getMessage("code." + key, null, Locale.ENGLISH);

        return Integer.parseInt(code);
    }

    public String getMessage(String key) {
        return this.messageSource.getMessage("message." + key, null, Locale.ENGLISH);
    }

    public String getMessage(String key, String... params) {
        return this.messageSource.getMessage("message." + key, params, Locale.ENGLISH);
    }
}
