package com.boraun.dashboard.admin.message;

import com.boraun.dashboard.common.CoreConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

@Component("messageSource")
@RequiredArgsConstructor
public class MessageUtilities extends AbstractMessageSource {

    private final MessageRepository repository;

    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        if (!isValidLocale(locale)) {
            locale = Locale.getDefault();
        }
        MessageEntity message = repository.findFirstByKeyAndLocaleAndStatus(key, locale.getLanguage(), CoreConstants.Status.Enabled);
        if (message == null) {
            message = new MessageEntity();
            message.setContent(key);
            message.setKey(key);
            message.setStatus(CoreConstants.Status.Enabled);
            message.setLocale(locale.getLanguage());
            message = repository.saveAndFlush(message);
        }
        return new MessageFormat(message.getContent(), locale);
    }

    private boolean isValidLocale(Locale locale) {
        return Arrays.stream(Locale.getISOLanguages()).anyMatch(lang -> lang.equals(locale.getLanguage()));
    }

    private String getString(String key, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return resolveCode(key, locale).toPattern();
    }

    public String getString(String key) {
        try {
            Locale locale = new Locale("en");
            return this.getString(key, locale);
        } catch (Exception ex) {
            return key;
        }
    }

    public boolean containKey(String key) {
        Locale locale = new Locale("en");
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        return messages.containsKey(key);
    }
}
