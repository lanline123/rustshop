package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批量设置批发价的阶梯行（B6/B7 tiers[] 元素）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发价阶梯行")
public class WholesaleBatchTierDTO {

    @NotNull(message = "minQty 不能为空")
    @Schema(description = "阶梯起量，≥1", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer minQty;

    @Schema(description = "折率 [0.10, 9.99]，priceMode=1 时用", example = "5.00")
    private BigDecimal discount;

    @Schema(description = "固定批发价 >0，priceMode=2 时用", example = "600.99")
    private BigDecimal wholesalePrice;
}
