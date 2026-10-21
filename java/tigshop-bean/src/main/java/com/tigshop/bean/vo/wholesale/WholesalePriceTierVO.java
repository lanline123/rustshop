// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.wholesale;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * C 端商品详情批发阶梯价 VO（TASK-WS-210 / CR-20260602-162000 D5 扩张）。
 *
 * <p>对应 api-contract §4.0 `WholesalePriceTierVO`。
 * 返回时机：批发商身份访问 {@code GET /api/product/product/detail} 时由 C 端 backfill；
 * 非批发商不返回字段。详情阶梯不按当前等级过滤（CD-022），UI 高亮当前等级行。
 *
 * <p>价格语义：
 * <ul>
 *   <li>{@code priceMode = 1}（折扣）：{@code discount} 为十分制（CD-015），
 *       {@code displayPrice = retailPrice × discount / 10}（HALF_UP，2 位）。</li>
 *   <li>{@code priceMode = 2}（固定）：{@code fixedPrice} 直接生效，
 *       {@code displayPrice = fixedPrice}。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "C 端商品详情批发阶梯价")
public class WholesalePriceTierVO {

    @Schema(description = "批发价主键 ID（对齐 admin 录入顺序的隐含排序）")
    private Integer priceId;

    @Schema(description = "阶梯起量")
    private Integer tierMinQty;

    @Schema(description = "价格模式：1=折扣换算 2=固定金额（对齐 WholesalePriceModeEnum）")
    private Integer priceMode;

    @Schema(description = "折率（十分制 [0.10, 9.99]，priceMode=1 时有效）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal discount;

    @Schema(description = "固定批发价（priceMode=2 时有效）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal fixedPrice;

    @Schema(description = "等级 ID；0=通用价")
    private Integer gradeId;

    @Schema(description = "等级名称；通用价时为「通用」")
    private String gradeName;

    @Schema(description = "SKU ID；0=SPU 兜底（全 SKU 通用）")
    private Integer skuId;

    @Schema(description = "最终展示价（priceMode=1 时按 retailPrice × discount / 10 计算；priceMode=2 时 = fixedPrice）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal displayPrice;
}
