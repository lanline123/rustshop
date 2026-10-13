package com.tigshop.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tigshop.common.annotation.SensitiveJson;
import com.tigshop.common.properties.TigshopProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * JSONObject 字段脱敏序列化器。
 * 将 JSONObject（实现了 Map 接口）中指定 key 的手机号值进行脱敏。
 * 仅在 tigshop.is-demo=1 时生效。
 */
@Component
public class SensitiveJsonSerializer extends StdSerializer<Object> implements ContextualSerializer, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private final Set<String> sensitiveKeys;

    public SensitiveJsonSerializer() {
        this(Collections.emptySet());
    }

    public SensitiveJsonSerializer(Set<String> sensitiveKeys) {
        super(Object.class);
        this.sensitiveKeys = sensitiveKeys;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        boolean shouldMask = isDemoEnabled() && !sensitiveKeys.isEmpty();

        if (value instanceof Map<?, ?> map) {
            gen.writeStartObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                gen.writeFieldName(key);
                Object val = entry.getValue();
                if (shouldMask && sensitiveKeys.contains(key) && val instanceof String str && !str.isEmpty()) {
                    gen.writeString(maskPhone(str));
                } else if (val instanceof Map) {
                    serialize(val, gen, provider);
                } else {
                    provider.defaultSerializeValue(val, gen);
                }
            }
            gen.writeEndObject();
        } else {
            provider.defaultSerializeValue(value, gen);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            SensitiveJson annotation = property.getAnnotation(SensitiveJson.class);
            if (annotation != null && annotation.keys().length > 0) {
                return new SensitiveJsonSerializer(new HashSet<>(Arrays.asList(annotation.keys())));
            }
        }
        return new SensitiveJsonSerializer(Collections.emptySet());
    }

    private boolean isDemoEnabled() {
        if (applicationContext == null) {
            return false;
        }
        try {
            TigshopProperties props = applicationContext.getBean(TigshopProperties.class);
            return props != null && props.getIsDemo() != null && Objects.equals(props.getIsDemo(), 1);
        } catch (Exception e) {
            return false;
        }
    }

    private static String maskPhone(String phone) {
        if (phone.length() < 7) {
            return phone.charAt(0) + "****" + phone.charAt(phone.length() - 1);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
