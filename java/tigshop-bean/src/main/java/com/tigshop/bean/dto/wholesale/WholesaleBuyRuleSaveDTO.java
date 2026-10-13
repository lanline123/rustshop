package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 起批量设置入参（图8）。
 *
 * <p>CR-20260604-0937：admin UI 简化为只填一个 {@code moq} 数字。
 * 不论 {@code dimension=1}（SPU）或 {@code dimension=2}（SKU），都使用同一字段；
 * SKU 维度时该数字应用到该商品所有 SKU。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "起批量设置入参")
public class WholesaleBuyRuleSaveDTO {

    @NotNull(message = "productId 不能为空")
    @Positive(message = "productId 必须为正数")
    @Schema(description = "product_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30140")
    private Integer productId;

    @NotNull(message = "dimension 不能为空")
    @Schema(description = "起批维度：1=SPU 2=SKU", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dimension;

    @NotNull(message = "moq 不能为空")
    @Min(value = 1, message = "moq 必须 ≥ 1")
    @Schema(description = "起批数：dimension=1 时为加总起批；dimension=2 时为每 SKU 起批（应用到所有 SKU），≥1",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer moq;
}
