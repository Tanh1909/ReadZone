package vn.tnteco.common.config.locale;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Translator {

    @Setter
    private static ResourceBundleMessageSource messageSource;

    Translator(@Autowired(required = false) ResourceBundleMessageSource messageSource) {
        setMessageSource(messageSource);
    }

    public static String toLocale(String messageCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageCode, (Object[]) null, locale);
    }

    public static String toLocale(String messageCode, String[] params) {
        Locale locale = LocaleContextHolder.getLocale();
        if (params != null && params.length > 0) {
            Object[] translatedParams = new Object[params.length];
            for (int i = 0; i < translatedParams.length; i++) {
                translatedParams[i] = messageSource.getMessage(params[i], null, locale);
            }
            return messageSource.getMessage(messageCode, translatedParams, locale);
        }
        return messageSource.getMessage(messageCode, params, locale);
    }
}