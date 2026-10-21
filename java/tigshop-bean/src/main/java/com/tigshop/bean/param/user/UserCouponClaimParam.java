package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 优惠劵领取参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "优惠劵领取参数")
public class UserCouponClaimParam {

    @NotNull(message = "优惠券主键不能为空")
    @Schema(description = "优惠券主键")
    private Integer couponId;

    @Schema(description = "用户id")
    private Integer userId;

}
