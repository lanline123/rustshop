package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 微信直播同步回调
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Data
@Schema(description = "微信直播同步回调")
public class WechatLiveSyncCallbackDTO {

    @NotBlank(message = "eventType不能为空")
    @Schema(description = "事件类型")
    private String eventType;

    @NotBlank(message = "idempotentKey不能为空")
    @Schema(description = "幂等键")
    private String idempotentKey;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款ID")
    private Integer refundId;

    @Schema(description = "直播房间ID")
    private Integer roomId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "附加消息")
    private String message;

    @Schema(description = "回调载荷")
    private Map<String, Object> payload;
}
