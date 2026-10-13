package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 倍数购买保存入参（图9）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "倍数购买保存入参")
public class WholesaleMultipleSaveDTO {

    @NotNull(message = "productId 不能为空")
    @Positive(message = "productId 必须为正数")
    @Schema(description = "product_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30140")
    private Integer productId;

    @Valid
    @NotEmpty(message = "skuMultiples 不能为空")
    @Schema(description = "各 SKU 倍数配置（永远按 SKU 配置，C12）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<WholesaleMultipleSkuDTO> skuMultiples;
}
