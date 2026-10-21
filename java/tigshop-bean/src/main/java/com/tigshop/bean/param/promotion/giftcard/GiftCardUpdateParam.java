package com.tigshop.bean.param.promotion.giftcard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop项目组
 * @create 2025年10月29日 10:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "卡创建参数")
public class GiftCardUpdateParam {

    @Schema(description = "卡ID")
    private Integer cardId;

    @Schema(description = "卡密码")
    private String cardPassword;

    @Schema(description = "1-可用 0-不可用")
    private Integer isAvailable;
}