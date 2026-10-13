package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 倍数购买的 SKU 行（B9 skuMultiples[] 元素）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "SKU 倍数行")
public class WholesaleMultipleSkuDTO {

    @NotNull(message = "skuId 不能为空")
    @Min(value = 0, message = "skuId 不能为负数")
    @Schema(description = "SKU ID（单规格商品传 0 = SPU 兜底，命中全部 SKU；多规格传具体 SKU id）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "5120")
    private Integer skuId;

    @NotNull(message = "multiple 不能为空")
    @Schema(description = "倍数购买，1=不限制", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer multiple;
}
