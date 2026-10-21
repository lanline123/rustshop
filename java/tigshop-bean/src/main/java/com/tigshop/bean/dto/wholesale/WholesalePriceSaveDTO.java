package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 批发价保存入参（单规格 + 多规格统一）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发价保存入参")
public class WholesalePriceSaveDTO {

    @NotNull(message = "productId 不能为空")
    @Positive(message = "productId 必须为正数")
    @Schema(description = "已存在 + 已配批发的 product_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30140")
    private Integer productId;

    @Schema(description = "多规格价格模式：1=商品级混批 2=SKU级；单规格商品忽略", example = "2")
    private Integer multiSpecPriceMode;

    @Valid
    @NotEmpty(message = "priceRows 不能为空")
    @Schema(description = "批发价行（1-1000 行），保存语义=全量替换该 productId 所有 price 行", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<WholesalePriceRowDTO> priceRows;
}
