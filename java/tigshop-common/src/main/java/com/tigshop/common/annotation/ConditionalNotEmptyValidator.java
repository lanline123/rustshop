package com.tigshop.common.annotation;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * 条件不等于空校验器
 *
 * @author kidd
 * @since 2025/7/10 09:29
 */
public class ConditionalNotEmptyValidator implements ConstraintValidator<ConditionalNotEmpty, Object> {

    private String field;
    private String conditionField;
    private String conditionValue;

    @Override
    public void initialize(ConditionalNotEmpty constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.conditionField = constraintAnnotation.conditionField();
        this.conditionValue = constraintAnnotation.conditionValue();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        try {
            // 通过反射获取条件字段值
            Object conditionFieldValue = BeanUtil.getProperty(obj, this.conditionField);
            if (conditionFieldValue != null && !conditionFieldValue.toString().equals(this.conditionValue)) {
                // 不满足条件，不触发校验
                return true;
            }

            // 获取目标字段值
            Object fieldValue = BeanUtil.getProperty(obj, this.field);
            if (fieldValue == null) {
                return false;
            }

            if (fieldValue instanceof Collection<?>) {
                // 如果目标字段是集合，则判断集合是否为空
                return CollUtil.isNotEmpty((Collection<?>) fieldValue);
            }

            return true;
        } catch (Exception e) {
            // 反射失败，默认通过
            return true;
        }
    }
}
