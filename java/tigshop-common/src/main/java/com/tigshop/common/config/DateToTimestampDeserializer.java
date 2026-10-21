package com.tigshop.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.tigshop.common.annotation.TimestampFormat;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 日期字符串反序列化为秒级时间戳
 * 自动识别多种日期格式，同时兼容纯数字时间戳
 */
public class DateToTimestampDeserializer extends JsonDeserializer<Long> implements ContextualDeserializer {

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );

    private static final List<DateTimeFormatter> DATE_ONLY_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    private String preferredPattern;
    private String timezone;

    public DateToTimestampDeserializer() {
        this.timezone = ZoneId.systemDefault().getId();
    }

    public DateToTimestampDeserializer(String preferredPattern, String timezone) {
        this.preferredPattern = preferredPattern;
        this.timezone = (timezone == null || timezone.isEmpty()) ? ZoneId.systemDefault().getId() : timezone;
    }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        value = value.trim();

        // 纯数字：秒级或毫秒级时间戳
        if (value.matches("^\\d+$")) {
            long num = Long.parseLong(value);
            // 13位视为毫秒级，自动转秒级
            return num > 9999999999L ? num / 1000 : num;
        }

        ZoneId zone = ZoneId.of(timezone);

        // ISO 8601 带时区（如 2026-03-19T10:00:00+08:00 / 2026-03-19T02:00:00Z）
        try {
            return ZonedDateTime.parse(value).toEpochSecond();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(value).toEpochSecond();
        } catch (DateTimeParseException ignored) {
        }

        // 优先使用注解指定的 pattern
        if (preferredPattern != null && !preferredPattern.isEmpty()) {
            try {
                LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(preferredPattern));
                return dt.atZone(zone).toEpochSecond();
            } catch (DateTimeParseException ignored) {
            }
        }

        // 带时分秒的格式
        for (DateTimeFormatter fmt : DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime dt = LocalDateTime.parse(value, fmt);
                return dt.atZone(zone).toEpochSecond();
            } catch (DateTimeParseException ignored) {
            }
        }

        // 仅日期格式（补 00:00:00）
        for (DateTimeFormatter fmt : DATE_ONLY_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(value, fmt);
                return date.atStartOfDay(zone).toEpochSecond();
            } catch (DateTimeParseException ignored) {
            }
        }

        return 0L;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            TimestampFormat annotation = property.getAnnotation(TimestampFormat.class);
            if (annotation != null) {
                return new DateToTimestampDeserializer(annotation.value(), annotation.timezone());
            }
        }
        return this;
    }
}
