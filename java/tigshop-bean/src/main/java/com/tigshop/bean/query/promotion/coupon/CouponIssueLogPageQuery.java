package com.tigshop.bean.query.promotion.coupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 优惠券发放日志查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "优惠券发放日志查询参数")
public class CouponIssueLogPageQuery extends BasePage {

    @Schema(description = "优惠券活动ID")
    private Integer couponId;

    @Schema(description = "发放批次ID")
    private Long batchId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "发放来源：0-用户领取，1-抽奖中奖，2-手动发放")
    private Integer sourceType;
}
