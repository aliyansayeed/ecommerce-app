package uniblox.ai.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.context.i18n.LocaleContextHolder;

@Component
@RequiredArgsConstructor
public class MessageSourceUtils {
    private final MessageSource messageSource;

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

}
