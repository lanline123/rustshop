package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 添加批发商品入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "添加批发商品入参")
public class WholesaleProductAddDTO {

    @NotNull(message = "productId 不能为空")
    @Positive(message = "productId 必须为正数")
    @Schema(description = "已存在的 product_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30140")
    private Integer productId;

    @Schema(description = "多规格价格模式：1=商品级混批（默认） 2=SKU级独立；单规格商品忽略", example = "1")
    private Integer multiSpecPriceMode;

    @Schema(description = "起批维度：1=SPU（默认） 2=SKU", example = "1")
    private Integer moqDimension;
}
