package com.tigshop.bean.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单状态统计数量
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "订单状态统计数量")
public class OrderStatusCountVO {

    @Schema(description = "全部（不含已删除）")
    private Long allCount = 0L;

    @Schema(description = "待支付（状态 0）")
    private Long pendingCount = 0L;

    @Schema(description = "待发货（状态 1）")
    private Long confirmedCount = 0L;

    @Schema(description = "已发货（状态 2）")
    private Long processingCount = 0L;

    @Schema(description = "已取消（状态 3）")
    private Long cancelledCount = 0L;

    @Schema(description = "已完成（状态 5）")
    private Long completedCount = 0L;

    @Schema(description = "已删除（虚拟状态 -2，is_del=1）")
    private Long deletedCount = 0L;
}
