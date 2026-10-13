package com.tigshop.common.aspect;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.common.annotation.RetryLimit;
import com.tigshop.common.utils.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Tigshop
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryLimitAspect {

    private final RedisCache redisCache;

    @Around("@annotation(retryLimit)")
    public Object around(ProceedingJoinPoint pjp, RetryLimit retryLimit) throws Throwable {
        Object[] args = pjp.getArgs();

        // 用消息内容生成唯一 key（类似你现在的 unionId）
        String key = buildKey(pjp, args);
        log.error("用消息内容生成唯一 key：{}", key);

        Integer count = redisCache.getCacheObject(key);
        if (count != null && count >= retryLimit.maxAttempts()) {
            log.error("消息重试已达最大次数：{}", JSONUtil.toJsonStr(args));
            if (retryLimit.saveToDbOnFail()) {
                saveFail(key, args, count);
            }
            return null;
        }

        // 更新计数
        redisCache.setCacheObject(key, (count == null ? 1 : count + 1),
                retryLimit.ttlMinutes(), TimeUnit.MINUTES);

        try {
            return pjp.proceed();
        } catch (Exception e) {
            // 抛出给 Spring-Rabbit，让它走你配置的 ack 策略
            log.error("消费异常：{}", e.getMessage(), e);
            throw e;
        }
    }

    private void saveFail(String key, Object[] args, Integer count) {
        log.error("保存消费失败消息：{}", key);
    }

    private String buildKey(ProceedingJoinPoint pjp, Object[] args) {
        String methodName = pjp.getSignature().toShortString();
        String argsJson = JSONUtil.toJsonStr(args);
        return "MQ_RETRY_" + DigestUtil.md5Hex(methodName + ":" + argsJson);
    }
}
