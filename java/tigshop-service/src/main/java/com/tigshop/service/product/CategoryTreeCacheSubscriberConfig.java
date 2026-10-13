package com.tigshop.service.product;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 将 {@link CategoryTreeCache} 订阅到 Redis {@link CategoryTreeCache#INVALIDATE_CHANNEL}，
 * 实现 tigshop-adminapi ↔ tigshop-api 跨 JVM 的分类树快照失效广播。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CategoryTreeCacheSubscriberConfig {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final CategoryTreeCache categoryTreeCache;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
                categoryTreeCache,
                new ChannelTopic(CategoryTreeCache.INVALIDATE_CHANNEL));
        log.info("[CategoryTreeCache] 已订阅 Redis 频道 {}", CategoryTreeCache.INVALIDATE_CHANNEL);
    }
}
