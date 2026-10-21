package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券发放批次VO
 */
@Data
@Schema(description = "优惠券发放批次VO")
public class CouponIssueBatchVO {

    @Schema(description = "批次ID")
    private Long id;

    @Schema(description = "优惠券活动ID")
    private Integer couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "发放用户数")
    private Integer userCount;

    @Schema(description = "发放张数")
    private Integer couponCount;

    @Schema(description = "发放时间（时间戳）")
    private Long issueTime;

    @Schema(description = "发放来源：0-用户领取，1-抽奖中奖，2-手动发放")
    private Integer sourceType;

    @Schema(description = "发放来源名称")
    private String sourceTypeName;
}
