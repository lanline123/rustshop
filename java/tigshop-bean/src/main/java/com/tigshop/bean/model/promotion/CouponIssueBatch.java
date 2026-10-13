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
 * 优惠券发放批次
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon_issue_batch")
@Schema(description = "优惠券发放批次")
public class CouponIssueBatch implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "批次ID")
    private Long id;

    @Schema(description = "优惠券活动ID")
    private Integer couponId;

    @Schema(description = "发放用户数")
    private Integer userCount;

    @Schema(description = "发放张数")
    private Integer couponCount;

    @Schema(description = "发放时间（时间戳）")
    private Long issueTime;

    @Schema(description = "发放来源：0-用户领取，1-抽奖中奖，2-手动发放")
    private Integer sourceType;
}
