package com.tigshop.bean.param.promotion.giftcard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * @author Tigshop项目组
 * @create 2025年10月31日 10:26
 */
@lombok.Data
@Schema(description = "礼品卡组id参数")
public class GiftCardGroupIdParam {
    @Schema(description = "礼品卡id")
    @NotNull
    private Integer giftCardGroupsId;
}