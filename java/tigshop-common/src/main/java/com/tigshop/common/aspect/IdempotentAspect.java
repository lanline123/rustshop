package com.tigshop.common.aspect;

import com.tigshop.common.annotation.Idempotent;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 幂等性切面
 * 用于防止重复提交
 * 
 * @author Tigshop
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotentAspect {

    private final RedisCache redisCache;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        
        // 获取参数名和参数值
        String[] parameterNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        
        // 解析SpEL表达式获取key值
        String keyValue = parseKey(idempotent.key(), parameterNames, args);
        
        // 构建Redis key
        String prefix = idempotent.prefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = method.getDeclaringClass().getName() + "." + method.getName();
        }
        String redisKey = "idempotent:" + prefix + ":" + keyValue;
        

        // 尝试设置Redis key（使用tryLock实现幂等性）
        boolean success = redisCache.tryLock(redisKey, "1", idempotent.expireTime(), idempotent.timeUnit());
        
        if (!success) {
            // key已存在，说明正在处理中或已处理
            throw new GlobalException(idempotent.message());
        }
        
        try {
            // 执行业务方法
            return pjp.proceed();
        }finally {
            //不管业务执行是否成功,只要进入到这里都得进行删除掉
            redisCache.deleteObject(redisKey);
        }
    }
    
    /**
     * 解析SpEL表达式
     */
    private String parseKey(String keyExpression, String[] parameterNames, Object[] args) {
        // 构建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        
        // 将参数名和参数值放入上下文
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        
        // 解析表达式
        Expression expression = parser.parseExpression(keyExpression);
        Object value = expression.getValue(context);
        
        return value != null ? value.toString() : "";
    }
}

