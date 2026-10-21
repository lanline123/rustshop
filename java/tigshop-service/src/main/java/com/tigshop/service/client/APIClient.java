// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.client;

import com.tigshop.common.constant.im.ImPushConstants;
import com.tigshop.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 客服 IM 实时消息推送客户端（跨进程 / 跨节点）。
 * <p>
 * 通过 Redis Pub/Sub 将消息广播到所有 tigshop-api 节点，各节点查本机 WebSocket 会话推送，
 * 以支持多节点集群部署（替代原"HTTP 打单个 api 节点 /api/socket/send"的方式）。
 *
 * @author Tigshop团队
 * @create 2025年03月11日 13:27
 */
@Service
@Slf4j
public class APIClient {

    /**
     * StringRedisTemplate 由 spring-boot-starter-data-redis 自动装配；
     * required=false 保证 Redis 未配置时降级为"仅本地不推送"，不阻断主流程。
     */
    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 向指定用户发送 WebSocket 消息（发布到 Redis 广播频道，由各 tigshop-api 节点消费推送）。
     *
     * @param targetUserId 目标用户ID，格式为 "isAdmin::userId"
     * @param message      消息内容
     * @return 发送结果
     */
    public String sendMessage(String targetUserId, String message) {
        StringRedisTemplate redis = this.stringRedisTemplate;
        if (redis == null) {
            log.warn("[ImPush] StringRedisTemplate 未注入，实时推送跳过 targetUserId={}", targetUserId);
            return "redis unavailable";
        }
        try {
            String payload = JsonUtil.toJson(new ImPushConstants.Envelope(targetUserId, message));
            redis.convertAndSend(ImPushConstants.IM_PUSH_CHANNEL, payload);
            return "published";
        } catch (Exception e) {
            log.warn("[ImPush] 发布实时推送失败 targetUserId={}", targetUserId, e);
            return "publish error";
        }
    }
}
