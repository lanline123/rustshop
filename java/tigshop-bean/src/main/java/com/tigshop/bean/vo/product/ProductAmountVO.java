// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.product;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/4/2 17:02
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品数量")
public class ProductAmountVO {

    private BigDecimal total;
    private Integer count;

    /**
     * 是否走了批发路径（CR-20260604-1015）。
     *
     * <p>{@code true}：至少有一行命中批发价；{@code total} 已为"批发命中行批发价小计 + 未命中行零售价小计"。
     * <p>{@code false}：非批发商 / 非平台商品 / 该商品未配置批发能力 / 全部 SKU 行未命中 → 与零售路径完全一致。
     */
    @Schema(description = "是否走了批发路径（至少一行命中批发价）")
    private Boolean wholesale;

    /**
     * 批发命中行的小计金额（CR-20260604-1015）。
     *
     * <p>仅披露批发部分；前端可用 {@code total - wholesaleSubtotal} 反推零售部分。
     * {@code wholesale=false} 时为 {@code null}。
     */
    @Schema(description = "批发命中行的小计金额；wholesale=false 时为 null")
    private BigDecimal wholesaleSubtotal;

    /**
     * 批发软提示文案（CR-20260604-1015）。
     *
     * <p>起批量未达 / 倍数不满足时合成的文案；多行去重后用「；」拼接。
     * <p><b>软提示语义</b>：本接口不抛 5320/5321；真正成交校验在 cart/add + order/submit。
     * 无提示时为 {@code null}。
     */
    @Schema(description = "批发软提示文案（起批量 / 倍数等；多行合并，无提示时为 null）")
    private String hint;
}
