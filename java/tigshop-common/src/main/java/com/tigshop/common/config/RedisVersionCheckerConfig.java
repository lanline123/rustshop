package com.tigshop.common.config;

import com.tigshop.common.properties.TigshopProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Tigshop项目组
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVersionCheckerConfig implements ApplicationRunner {

    private static final String REDIS_VERSION_KEY = "app:version";

    private final StringRedisTemplate redisTemplate;

    private final TigshopProperties tigshopProperties;

    @Override
    public void run(ApplicationArguments args) {
        String localVersion = tigshopProperties.getVersionNum();
        String redisVersion = redisTemplate.opsForValue().get(REDIS_VERSION_KEY);

        if (redisVersion == null || !redisVersion.equals(localVersion)) {
            // 清空Redis
            //redisTemplate.getConnectionFactory().getConnection().flushDb();
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                connection.serverCommands().flushDb();
                return null;
            });

            // 更新Redis中的版本号
            redisTemplate.opsForValue().set(REDIS_VERSION_KEY, localVersion);

            log.debug("Redis 数据已清空并更新版本号为：{}", localVersion);
        } else {
            log.debug("Redis 版本一致，无需清空");
        }
    }
}
