package com.tigshop.common.annotation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalNotEmptyValidator.class)
public @interface ConditionalNotEmpty {
    String message() default "字段不满足条件约束";

    /**
     * 要校验的字段
     */
    String field();
    /**
     * 条件字段
     */
    String conditionField();
    /**
     * 条件字段需要等于这个值时才校验 field
     */
    String conditionValue();

    Class<?>[] groups() default {};
    Class<? extends java.lang.Object>[] payload() default {};
}
