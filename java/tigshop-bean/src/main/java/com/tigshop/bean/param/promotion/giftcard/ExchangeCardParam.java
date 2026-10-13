package com.tigshop.bean.param.promotion.giftcard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop项目组
 * @create 2025年11月03日 11:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "礼品卡兑换参数")
public class ExchangeCardParam {
    @Schema(description = "礼品卡id")
    private Integer giftCardsId;

    @Schema(description = "礼品卡号")
    private String cardNumber;

    @Schema(description = "礼品卡密码")
    private String cardPassword;

    @Schema(description = "用户id")
    private Integer userId;
}