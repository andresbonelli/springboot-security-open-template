package com.spacecodee.springbootsecurityopentemplate.language;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.spacecodee.springbootsecurityopentemplate.exceptions.util.MessageParameterUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageParameterHandler {
    private final MessageUtilComponent messageUtilComponent;

    public String createMessage(String messageKey, String locale, Object... parameters) {
        log.debug("Creating message with key: '{}', locale: '{}', parameters: {}",
                messageKey, locale, Arrays.toString(parameters));

        Object[] validatedParams = MessageParameterUtil.validateAndCleanParams(parameters);
        return messageUtilComponent.getMessage(messageKey, locale, validatedParams);
    }

    public String createErrorMessage(String messageKey, String locale, Object... parameters) {
        log.debug("Creating error message with key: '{}', locale: '{}', parameters: {}",
                messageKey, locale, Arrays.toString(parameters));

        Object[] validatedParams = MessageParameterUtil.validateAndCleanParams(parameters);
        return messageUtilComponent.getMessage(messageKey, locale, validatedParams);
    }
}