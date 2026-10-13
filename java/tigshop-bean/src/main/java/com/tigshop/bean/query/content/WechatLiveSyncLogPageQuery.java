package com.tigshop.bean.query.content;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信直播同步日志查询参数
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Getter
@Setter
@Schema(description = "微信直播同步日志查询参数")
public class WechatLiveSyncLogPageQuery extends BasePage {

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款ID")
    private Integer refundId;

    @Schema(description = "直播房间ID")
    private Integer roomId;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "同步方向")
    private String direction;

    @Schema(description = "同步状态")
    private Integer syncStatus;
}
