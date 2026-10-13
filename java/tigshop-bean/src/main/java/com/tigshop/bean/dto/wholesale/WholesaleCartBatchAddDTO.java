package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * C 端批量加车入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "C 端批量加车入参")
public class WholesaleCartBatchAddDTO {

    @Valid
    @NotEmpty(message = "items 不能为空")
    @Size(max = 50, message = "items 最多 50 个")
    @Schema(description = "批量加车项（size 1-50）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    /**
     * 批量加车单项
     */
    @Data
    @Schema(description = "批量加车单项")
    public static class Item {

        @NotNull(message = "productId 不能为空")
        @Positive(message = "productId 必须为正数")
        @Schema(description = "商品 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30140")
        private Integer productId;

        @NotNull(message = "skuId 不能为空")
        @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5120")
        private Integer skuId;

        @NotNull(message = "quantity 不能为空")
        @Positive(message = "quantity 必须为正数")
        @Schema(description = "数量，≥1", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer quantity;
    }
}
