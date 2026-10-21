package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 限时折扣model
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("time_discount")
@Schema(description = "限时折扣")
public class TimeDiscount {

    @TableId(value = "discount_id", type = IdType.AUTO)
    @Schema(description = "活动ID")
    private Integer discountId;

    @Schema(description = "活动名称")
    private String promotionName;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;
}