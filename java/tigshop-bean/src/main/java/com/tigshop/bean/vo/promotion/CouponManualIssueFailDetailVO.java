package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券手动发放失败明细
 */
@Data
@Schema(description = "优惠券手动发放失败明细")
public class CouponManualIssueFailDetailVO {

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员名称")
    private String memberName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "原因")
    private String reason;
}
