package com.tigshop.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.DateToTimestampDeserializer;
import com.tigshop.common.config.TimestampToDateFormatSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 时间戳与日期字符串双向转换注解
 * 序列化：Long时间戳 → 日期字符串（输出）
 * 反序列化：日期字符串 → Long时间戳（输入，同时兼容纯数字时间戳）
 *
 * @author Tigshop团队
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = TimestampToDateFormatSerializer.class)
@JsonDeserialize(using = DateToTimestampDeserializer.class)
public @interface TimestampFormat {
    /**
     * 日期格式，默认为 yyyy-MM-dd HH:mm:ss
     */
    String value() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 时区，默认为 ZoneId.systemDefault().getId()
     */
    String timezone() default "";
}
