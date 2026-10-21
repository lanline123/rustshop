// ... existing code ...
package com.tigshop.common.config;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.tigshop.common.annotation.TimestampFormat;
import com.tigshop.common.utils.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Tigshop团队
 * @create 2025/6/13 15:16
 */
public class TimestampToDateFormatSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private String pattern = "yyyy-MM-dd HH:mm:ss";
    private String timezone = ZoneId.systemDefault().getId();

    public TimestampToDateFormatSerializer() {
    }

    public TimestampToDateFormatSerializer(String pattern, String timezone) {
        this.pattern = pattern;
        this.timezone = StringUtils.isBlank(timezone) ? ZoneId.systemDefault().getId() : timezone ;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
//            gen.writeString("");
            gen.writeNull();
            return;
        }

        try {
            long timestamp;

            // 支持多种类型：Integer, Long, String
            if (value instanceof Integer) {
                timestamp = ((Integer) value).longValue();
            } else if (value instanceof Long) {
                timestamp = (Long) value;
            } else if (value instanceof String) {
                timestamp = Long.parseLong((String) value);
            } else {
                timestamp = Long.parseLong(value.toString());
            }

            if (timestamp > 0) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    ZoneId.of(timezone)
                );
                String formatted = dateTime.format(DateTimeFormatter.ofPattern(pattern));
                gen.writeString(formatted);
            } else {
                gen.writeString("");
            }
        } catch (Exception e) {
            gen.writeString(value.toString());
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            TimestampFormat annotation = property.getAnnotation(TimestampFormat.class);
            if (annotation != null) {
                return new TimestampToDateFormatSerializer(annotation.value(), annotation.timezone());
            }
        }
        return this;
    }
}
