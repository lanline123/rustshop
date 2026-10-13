package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 限时折扣活动商品
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("time_discount_item")
@Schema(description = "限时折扣")
public class TimeDiscountItem {

    @TableId(value = "item_id", type = IdType.AUTO)
    @Schema(description = "活动商品ID")
    private Integer itemId;

    @Schema(description = "关联折扣活动ID")
    private Integer discountId;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "规格ID")
    private String skuIds;

    @Schema(description = "促销价格，减的金额或打的折数")
    private BigDecimal value;

    @Schema(description = "1打折2减钱3促销")
    private Integer discountType;
}
