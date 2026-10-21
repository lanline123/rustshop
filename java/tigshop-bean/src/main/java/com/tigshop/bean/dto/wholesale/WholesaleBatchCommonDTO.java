package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 批量设置通用售价入参（图6）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批量设置通用售价入参")
public class WholesaleBatchCommonDTO {

    @NotNull(message = "productId 不能为空")
    @Positive(message = "productId 必须为正数")
    @Schema(description = "product_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30140")
    private Integer productId;

    @Schema(description = "应用到的 SKU；空=全 SKU")
    private List<Integer> skuIds;

    @NotNull(message = "priceMode 不能为空")
    @Schema(description = "1=折扣 2=固定", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer priceMode;

    @Valid
    @NotEmpty(message = "tiers 不能为空")
    @Schema(description = "阶梯行（应用到 grade_id=0 通用价）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<WholesaleBatchTierDTO> tiers;
}
