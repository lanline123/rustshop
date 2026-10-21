package com.tigshop.bean.dto.order;

import com.tigshop.bean.param.order.AftersalesApplyParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后管理参数")
public class ClientAfterSalesUpdateDTO {
    @Schema(description = "售后id")
    private Integer aftersaleId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "售后类型")
    private Integer aftersaleType;

    @Schema(description = "售后原因")
    private String aftersaleReason;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "反馈图片")
    private List<ReturnPicDTO> pics;

    @Schema(description = "items")
    private List<AftersalesApplyParam.ItemsDTO> items;

    @Schema(description = "用户id")
    private Integer userId;

    @Data
    public static class ItemsDTO {
        @Schema(description = "订单item_id")
        private Integer orderItemId;

        @Schema(description = "数量")
        private Integer number;
    }
}
