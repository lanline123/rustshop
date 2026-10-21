package com.tigshop.service.setting;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 将 {@link ConfigValCache} 订阅到 Redis {@link ConfigValCache#INVALIDATE_CHANNEL}，
 * 实现 tigshop-adminapi ↔ tigshop-api 跨 JVM 的配置值缓存失效广播。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ConfigValCacheSubscriberConfig {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final ConfigValCache configValCache;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
                configValCache,
                new ChannelTopic(ConfigValCache.INVALIDATE_CHANNEL));
        log.info("[ConfigValCache] 已订阅 Redis 频道 {}", ConfigValCache.INVALIDATE_CHANNEL);
    }
}
