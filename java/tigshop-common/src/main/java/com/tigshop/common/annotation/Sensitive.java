package com.tigshop.common.annotation;

import com.tigshop.common.enums.SensitiveType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解，标注在 VO 的 String 字段上。
 * 仅在 tigshop.is-demo=1 时生效，序列化输出时对字段值进行脱敏。
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    SensitiveType type();
}
