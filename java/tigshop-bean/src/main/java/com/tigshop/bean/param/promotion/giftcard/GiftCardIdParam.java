package com.tigshop.bean.param.promotion.giftcard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * @author Tigshop项目组
 * @create 2025年10月31日 10:26
 */
@lombok.Data
@Schema(description = "礼品卡id参数")
public class GiftCardIdParam {
    @Schema(description = "礼品卡id")
    @NotNull
    private String cardGroupsId;

    @Schema(description = "数量")
    private Integer count;

    public Long getCardGroupsId() {
        if (cardGroupsId == null) {
            return null;
        }
        String value = cardGroupsId.trim();
        if (value.isEmpty()) {
            return null;
        }
        return Long.parseLong(value);
    }

    public void setCardGroupsId(Long cardGroupsId) {
        this.cardGroupsId = cardGroupsId == null ? null : String.valueOf(cardGroupsId);
    }
}