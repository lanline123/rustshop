// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.common.service;

import com.anji.captcha.service.CaptchaCacheService;
import jakarta.annotation.Resource;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 自定义redis验证码缓存实现类
 *
 * @author Tigshop团队
 */
@Setter
@Service
public class CaptchaRedisService implements CaptchaCacheService {

    /*private final StringRedisTemplate redisTemplate = SpringContentUtils
            .getBean(StringRedisTemplate.class);*/

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean exists(String key) {
        if (redisTemplate != null) {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        }
        return false;
    }

    @Override
    public void delete(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public String get(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    @Override
    public Long increment(String key, long val) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().increment(key, val);
        }
        return null;
    }

    @Override
    public String type() {
        return "redis";
    }
}