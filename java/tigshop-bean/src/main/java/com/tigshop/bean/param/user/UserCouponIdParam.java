package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠劵id
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "优惠劵id")
public class UserCouponIdParam {

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "couponId")
    private Integer couponId;

}
