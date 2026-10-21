// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.im;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客服 IM 实时推送（Redis Pub/Sub）常量。
 *
 * <p>用于替代原"业务侧 HTTP 打单个 api 节点 /api/socket/send"的推送方式：
 * 业务侧将 {@link Envelope} 发布到 {@link #IM_PUSH_CHANNEL}，所有 tigshop-api 节点订阅该频道，
 * 各自查本机 WebSocket 会话表并推送（本机持有该会话就推、没有则忽略），从而支持多节点集群部署。
 *
 * <p>投递语义：at-most-once（即发即弃、无持久化/ACK），消息本体已入库 im_message，
 * 客户端重连/进入会话时按 DB 对齐历史与未读数兜底。
 *
 * @author Tigshop团队
 */
public class ImPushConstants {

    /**
     * 客服 IM 实时消息推送广播频道（跨节点 fan-out）。
     */
    public static final String IM_PUSH_CHANNEL = "im:message:push";

    private ImPushConstants() {
    }

    /**
     * 推送信封：一条广播消息同时承载"目标用户"与"消息内容"。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Envelope {

        /**
         * 目标用户ID，格式为 "isAdmin::userId"，例如 "false::123"（客户）或 "true::1"（客服）。
         * 与 WebSocketServerHandler.USER_SESSIONS 的 key 一致。
         */
        private String targetUserId;

        /**
         * 原始消息内容（已是 JSON 字符串）。
         */
        private String message;
    }
}
