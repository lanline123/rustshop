package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品起批规则 VO（C 端）。
 *
 * <p>CR-20260604-0937 契约简化：
 * <ul>
 *   <li>原 {@code moqSpu} → {@code moq}（统一字段，含义随 {@code dimension} 解释）</li>
 *   <li>{@code skuRules[].moqSku} 删除（不再有每 SKU 起批数），仅保留 {@code multiple}</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "商品起批规则")
public class WholesaleProductBuyRuleVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "是否配置批发能力")
    private Boolean configured;

    @Schema(description = "起批维度：1=SPU 2=SKU")
    private Integer dimension;

    @Schema(description = "起批数：dimension=1 时为加总起批；dimension=2 时为每 SKU 起批")
    private Integer moq;

    @Schema(description = "SKU 倍数购买规则（multiple 仍按 SKU 独立配置）")
    private List<WholesaleSkuRuleVO> skuRules;

    @Schema(description = "前端展示提示文案（后端组装）")
    private String hint;
}
