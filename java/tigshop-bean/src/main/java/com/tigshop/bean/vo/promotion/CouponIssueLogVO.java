package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券发放日志VO
 */
@Data
@Schema(description = "优惠券发放日志VO")
public class CouponIssueLogVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "发放批次ID")
    private Long batchId;

    @Schema(description = "优惠券活动ID")
    private Integer couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "用户优惠券ID")
    private Integer userCouponId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "发放时间（时间戳）")
    private Long issueTime;

    @Schema(description = "发放来源：0-用户领取，1-抽奖中奖，2-手动发放")
    private Integer sourceType;

    @Schema(description = "发放来源名称")
    private String sourceTypeName;

    @Schema(description = "是否已使用：0-未使用，1-已使用")
    private Integer used;

    @Schema(hidden = true)
    private Long usedTime;
}
