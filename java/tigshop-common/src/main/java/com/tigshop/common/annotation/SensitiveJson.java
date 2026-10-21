package com.tigshop.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.SensitiveJsonSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 JSONObject 字段上，序列化时对指定 key 的值进行手机号脱敏。
 * 仅在 tigshop.is-demo=1 时生效。
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveJsonSerializer.class)
public @interface SensitiveJson {

    /**
     * 需要脱敏的 key 名称列表
     */
    String[] keys();
}
