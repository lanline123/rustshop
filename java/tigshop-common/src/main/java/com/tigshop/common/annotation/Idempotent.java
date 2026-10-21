package com.tigshop.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解
 * 用于防止重复提交
 * 
 * @author Tigshop
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    
    /**
     * 幂等性Key的前缀
     * 默认为方法全路径名
     */
    String prefix() default "";
    
    /**
     * 幂等性Key的参数字段名
     * 支持SpEL表达式，例如：#param.id
     */
    String key();
    
    /**
     * 过期时间
     * 默认10分钟
     */
    long expireTime() default 10;
    
    /**
     * 过期时间单位
     * 默认为分钟
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
    
    /**
     * 提示信息
     */
    String message() default "操作正在处理中，请勿重复提交";
}

