package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 优惠券发放日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon_issue_log")
@Schema(description = "优惠券发放日志")
public class CouponIssueLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "发放批次ID")
    private Long batchId;

    @Schema(description = "优惠券活动ID（coupon表主键）")
    private Integer couponId;

    @Schema(description = "用户优惠券ID（user_coupon表主键）")
    private Integer userCouponId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "发放时间（时间戳）")
    private Long issueTime;

    @Schema(description = "发放来源：0-用户领取，1-抽奖中奖，2-手动发放")
    private Integer sourceType;
}
