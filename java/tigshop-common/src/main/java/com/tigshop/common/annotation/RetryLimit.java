package com.tigshop.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tigshop
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryLimit {
    // 最大重试次数
    int maxAttempts() default 3;
    // 计数过期时间
    int ttlMinutes() default 1;
    // 是否落库
    boolean saveToDbOnFail() default true;
}
