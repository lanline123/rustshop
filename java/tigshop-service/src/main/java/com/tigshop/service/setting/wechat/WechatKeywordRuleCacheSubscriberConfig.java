// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import jakarta.annotation.PostConstruct;

/**
 * 把 {@link WechatKeywordRuleCache} 订阅到 Redis
 * {@link WechatKeywordRuleCache#INVALIDATE_CHANNEL} 频道，
 * 实现跨 JVM（{@code tigshop-adminapi} ↔ {@code tigshop-api}）的缓存失效广播。
 *
 * <p>之所以独立成一个 Configuration、不直接写进 {@code RedisConfig}：
 * <ul>
 *   <li>{@code RedisConfig} 在 common 模块，而 {@link WechatKeywordRuleCache} 在 service 模块，
 *       反向依赖不合适。</li>
 *   <li>订阅关系是业务层关注点，与 Redis 本身无关，隔离在业务包里便于收敛维护。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月24日
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WechatKeywordRuleCacheSubscriberConfig {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final WechatKeywordRuleCache wechatKeywordRuleCache;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
                wechatKeywordRuleCache,
                new ChannelTopic(WechatKeywordRuleCache.INVALIDATE_CHANNEL));
        log.info("[KeywordRuleCache] 已订阅 Redis 频道 {}", WechatKeywordRuleCache.INVALIDATE_CHANNEL);
    }
}
