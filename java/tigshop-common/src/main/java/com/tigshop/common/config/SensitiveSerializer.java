package com.tigshop.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.properties.TigshopProperties;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏 Jackson 序列化器。
 * 仅在 tigshop.is-demo=1 时对标注了 @Sensitive 的字段执行脱敏。
 */
public class SensitiveSerializer extends StdSerializer<String> implements ContextualSerializer {

    private final TigshopProperties tigshopProperties;
    private final SensitiveType sensitiveType;

    public SensitiveSerializer(TigshopProperties tigshopProperties) {
        this(tigshopProperties, null);
    }

    public SensitiveSerializer(TigshopProperties tigshopProperties, SensitiveType sensitiveType) {
        super(String.class);
        this.tigshopProperties = tigshopProperties;
        this.sensitiveType = sensitiveType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (sensitiveType != null && isDemoEnabled() && value != null && !value.isEmpty()) {
            gen.writeString(mask(value, sensitiveType));
        } else {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (annotation != null) {
                return new SensitiveSerializer(tigshopProperties, annotation.type());
            }
        }
        return new SensitiveSerializer(tigshopProperties, null);
    }

    private boolean isDemoEnabled() {
        return tigshopProperties != null
                && tigshopProperties.getIsDemo() != null
                && Objects.equals(tigshopProperties.getIsDemo(), 1);
    }

    private static String mask(String value, SensitiveType type) {
        return switch (type) {
            case PHONE -> maskPhone(value);
            case ID_CARD -> maskIdCard(value);
            case EMAIL -> maskEmail(value);
            case BANK_CARD -> maskBankCard(value);
        };
    }

    private static String maskPhone(String phone) {
        if (phone.contains("-")) {
            int idx = phone.indexOf('-');
            String areaCode = phone.substring(0, idx);
            String localNum = phone.substring(idx + 1);
            int len = localNum.length();
            if (len >= 8) {
                return areaCode + "-****" + localNum.substring(len - 4);
            } else if (len >= 7) {
                return areaCode + "-****" + localNum.substring(len - 3);
            } else if (len >= 2) {
                int keep = len / 2;
                return areaCode + "-****" + localNum.substring(len - keep);
            }
            return areaCode + "-****";
        }
        if (phone.length() < 7) {
            return phone.charAt(0) + "****" + phone.charAt(phone.length() - 1);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskIdCard(String idCard) {
        if (idCard.length() < 7) {
            return idCard.charAt(0) + "****" + idCard.charAt(idCard.length() - 1);
        }
        return idCard.substring(0, 3) + "****" + idCard.substring(idCard.length() - 4);
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private static String maskBankCard(String card) {
        if (card.length() < 8) {
            return card.charAt(0) + "****" + card.charAt(card.length() - 1);
        }
        return card.substring(0, 4) + "****" + card.substring(card.length() - 4);
    }
}
