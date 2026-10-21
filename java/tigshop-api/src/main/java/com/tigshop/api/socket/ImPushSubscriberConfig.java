// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.socket;

import com.tigshop.common.constant.im.ImPushConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 将 {@link ImPushMessageListener} 订阅到 Redis {@link ImPushConstants#IM_PUSH_CHANNEL}，
 * 实现客服 IM 实时消息的跨 JVM / 多节点广播投递。
 *
 * <p>复用 tigshop-common {@code RedisConfig} 提供的共享 {@link RedisMessageListenerContainer} 单例。
 *
 * @author Tigshop团队
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ImPushSubscriberConfig {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final ImPushMessageListener imPushMessageListener;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
                imPushMessageListener,
                new ChannelTopic(ImPushConstants.IM_PUSH_CHANNEL));
        log.info("[ImPush] 已订阅 Redis 频道 {}", ImPushConstants.IM_PUSH_CHANNEL);
    }
}
