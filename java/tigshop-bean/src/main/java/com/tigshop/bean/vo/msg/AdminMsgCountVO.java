package com.tigshop.bean.vo.msg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop团队
 * @create 2025/4/1 15:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "管理员消息统计")
public class AdminMsgCountVO {
    @Schema(description = "客服未读消息数量（用于 UI 角标，可随已读回落）")
    private Long imMsgCount;

    @Schema(description = "客服消息累计数量（自 startTime 起单调递增，专供语音播报增量判断）")
    private Long customerMsgCount;
    @Schema(description = "获取新订单数量")
    private Long orderCount;
    @Schema(description = "获取未读消息数量")
    private Long unreadMsgCount;

    @Schema(description = "已付款待发货订单数量")
    private Long paidOrderCount;

    @Schema(description = "新增待处理售后申请数量")
    private Long aftersaleCount;

    @Schema(description = "已取消订单数量")
    private Long cancelOrderCount;

    @Schema(description = "新订单后台消息数(admin_msg msgType=11，自 startTime 起，与消息中心同源，专供语音播报)")
    private Long newOrderMsgCount;

    @Schema(description = "已付款后台消息数(admin_msg msgType=12，自 startTime 起，与消息中心同源，专供语音播报)")
    private Long paidOrderMsgCount;

    @Schema(description = "售后申请后台消息数(admin_msg msgType=32，自 startTime 起，与消息中心同源，专供语音播报)")
    private Long aftersaleMsgCount;

    @Schema(description = "订单取消后台消息数(admin_msg msgType=31，自 startTime 起，与消息中心同源，专供语音播报)")
    private Long cancelOrderMsgCount;

    @Schema(description = "帧生成时间戳（秒）")
    private Long ts;

    @Schema(description = "协议版本号，前端按 >=2 走新逻辑")
    private Integer version;

    @Schema(description = "当前管理员 ID")
    private Integer adminId;
}
