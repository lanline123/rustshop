package com.tigshop.common.utils;

import org.apache.ibatis.parsing.GenericTokenParser;

import java.util.Map;
import java.util.Optional;

public class MessageTemplateUtils {

    public static String fillMessageTemplate(String messageTemplate, Map<String, Object> params) {
        GenericTokenParser genericTokenParser = new GenericTokenParser("{", "}", (token) -> {
            return Optional.ofNullable(params.getOrDefault(token, token)).orElse("").toString();
        });
        return genericTokenParser.parse(messageTemplate);
    }

    public static String fillMessageTemplate(String openToken, String closeToken, String messageTemplate, Map<String, Object> params) {
        GenericTokenParser genericTokenParser = new GenericTokenParser(openToken, closeToken, (token) -> {
            return Optional.ofNullable(params.getOrDefault(token, token)).orElse("").toString();
        });
        return genericTokenParser.parse(messageTemplate);
    }
}
