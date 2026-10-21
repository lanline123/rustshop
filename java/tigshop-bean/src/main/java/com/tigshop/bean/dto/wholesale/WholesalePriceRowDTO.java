package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批发价行入参（B5 priceRows[] 的元素）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发价行入参")
public class WholesalePriceRowDTO {

    @NotNull(message = "skuId 不能为空")
    @Schema(description = "≥0；0=SPU兜底；>0=必须属于该 productId", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer skuId;

    @NotNull(message = "gradeId 不能为空")
    @Schema(description = "≥0；0=通用价；>0=启用中等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer gradeId;

    @NotNull(message = "tierMinQty 不能为空")
    @Schema(description = "阶梯起量，≥1", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer tierMinQty;

    @NotNull(message = "priceMode 不能为空")
    @Schema(description = "1=折扣 2=固定", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer priceMode;

    @Schema(description = "折率 [0.10, 9.99]，priceMode=1 时必填", example = "5.00")
    private BigDecimal discount;

    @Schema(description = "固定批发价 >0，priceMode=2 时必填", example = "600.99")
    private BigDecimal wholesalePrice;
}
