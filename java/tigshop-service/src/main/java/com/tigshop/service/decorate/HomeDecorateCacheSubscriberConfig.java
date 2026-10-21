package com.tigshop.service.decorate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 将 {@link HomeDecorateCache} 订阅到 Redis {@link HomeDecorateCache#INVALIDATE_CHANNEL}，
 * 实现 tigshop-adminapi ↔ tigshop-api 跨 JVM 的首页装修缓存失效广播。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class HomeDecorateCacheSubscriberConfig {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final HomeDecorateCache homeDecorateCache;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
                homeDecorateCache,
                new ChannelTopic(HomeDecorateCache.INVALIDATE_CHANNEL));
        log.info("[HomeDecorateCache] 已订阅 Redis 频道 {}", HomeDecorateCache.INVALIDATE_CHANNEL);
    }
}
