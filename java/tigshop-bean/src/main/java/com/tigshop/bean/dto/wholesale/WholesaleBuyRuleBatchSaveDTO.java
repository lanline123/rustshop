package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量起批量设置入参（admin 批发商品列表 — 批量设置起批量抽屉）。
 *
 * <p>CR-20260604-1042：一次给 N 个 product 应用同一对 {@code dimension + moq}。
 * 与单接口 {@link WholesaleBuyRuleSaveDTO} 共享同款字段语义（不再每 SKU 单独填）。
 *
 * <p>上限 50（与 {@link WholesaleProductBatchAddDTO} / cartBatchAdd 一致）。
 * 整批事务：任一 productId 未配置批发能力 → 整批回滚（不允许部分成功）。
 *
 * @author Tigshop团队
 * @create 2026-06-04
 */
@Data
@Schema(description = "批量起批量设置入参")
public class WholesaleBuyRuleBatchSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "商品 ID 列表不能为空")
    @Size(min = 1, max = 50, message = "批量数量必须在 1-50 之间")
    @Schema(description = "商品 ID 数组（1-50 个，元素必须为正整数）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[101,102,103,104]")
    private List<@Positive(message = "productId 必须为正数") Integer> productIds;

    @NotNull(message = "dimension 不能为空")
    @Schema(description = "起批维度：1=SPU 2=SKU",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dimension;

    @NotNull(message = "moq 不能为空")
    @Min(value = 1, message = "moq 必须 ≥ 1")
    @Schema(description = "起批数（≥1）；dimension=1 时为加总起批；dimension=2 时为每 SKU 起批（应用到所有 SKU）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer moq;
}
