package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 优惠券列表VO(抽奖活动选择用)
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "优惠券列表VO")
public class CouponListVO {

    @Schema(description = "优惠券ID")
    private Integer couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券类型:1-满减券,2-折扣券,3-无门槛券")
    private Integer couponType;

    @Schema(description = "优惠金额/折扣")
    private BigDecimal couponAmount;

    @Schema(description = "使用门槛(满X元可用)")
    private BigDecimal minAmount;

    @Schema(description = "有效期开始时间")
    private Long startTime;

    @Schema(description = "有效期结束时间")
    private Long endTime;

    @Schema(description = "发行总量")
    private Integer totalNum;

    @Schema(description = "已使用数量")
    private Integer usedNum;
}


