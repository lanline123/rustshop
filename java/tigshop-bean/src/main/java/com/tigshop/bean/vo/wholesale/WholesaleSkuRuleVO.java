package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SKU 倍数规则 VO。
 *
 * <p>CR-20260604-0937：删除 {@code moqSku} 字段。起批数统一到顶层 {@code moq}；
 * 本 VO 仅承载 {@code multiple}（倍数购买仍按 SKU 独立配置）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "SKU 倍数规则")
public class WholesaleSkuRuleVO {

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "倍数购买，1=不限制")
    private Integer multiple;
}
