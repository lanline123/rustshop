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
import com.tigshop.common.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 客服 IM 实时推送 Redis 订阅监听器。
 *
 * <p>所有 tigshop-api 节点都订阅 {@link ImPushConstants#IM_PUSH_CHANNEL}；收到广播后解析信封，
 * 只查本机 {@link WebSocketServerHandler} 的会话表推送——本机持有该用户会话就推、没有则忽略。
 * 由此实现多节点集群下的实时消息投递（替代原单节点 HTTP 推送）。
 *
 * @author Tigshop团队
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImPushMessageListener implements MessageListener {

    private final WebSocketServerHandler webSocketServerHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (message.getBody() == null) {
            return;
        }
        String payload;
        try {
            payload = new String(message.getBody(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[ImPush] 推送信封解码失败", e);
            return;
        }
        ImPushConstants.Envelope envelope = JsonUtil.fromJson(payload, ImPushConstants.Envelope.class);
        if (envelope == null || envelope.getTargetUserId() == null) {
            log.warn("[ImPush] 推送信封为空或缺目标, payload={}", payload);
            return;
        }
        webSocketServerHandler.sendMessageToUser(envelope.getTargetUserId(), envelope.getMessage());
    }
}
