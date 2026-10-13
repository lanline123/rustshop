package com.tigshop.bean.param.promotion.giftcard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年10月31日 19:33
 */
@Data
@Schema(description = "礼品卡领取参数")
public class GiftCardClaimParam {

    @Schema(description = "礼品卡序列号")
    private String giftCardSn;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "来源")
    private Integer isFrom;
}