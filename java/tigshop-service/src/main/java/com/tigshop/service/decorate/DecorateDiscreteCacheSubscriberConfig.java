package com.tigshop.service.decorate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 将 {@link DecorateDiscreteCache} 订阅到 Redis {@link DecorateDiscreteCache#INVALIDATE_CHANNEL}，
 * 实现 tigshop-adminapi ↔ tigshop-api 跨 JVM 的离散组件缓存失效广播。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DecorateDiscreteCacheSubscriberConfig {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final DecorateDiscreteCache decorateDiscreteCache;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
                decorateDiscreteCache,
                new ChannelTopic(DecorateDiscreteCache.INVALIDATE_CHANNEL));
        log.info("[DecorateDiscreteCache] 已订阅 Redis 频道 {}", DecorateDiscreteCache.INVALIDATE_CHANNEL);
    }
}
