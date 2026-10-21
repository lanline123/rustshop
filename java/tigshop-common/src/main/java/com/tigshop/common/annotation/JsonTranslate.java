package com.tigshop.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tigshop团队
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonTranslate {
    // 默认取字段名（如 productName）作为 source
    String source() default "__FIELD_NAME__";

    int dataType() default 1;
}
