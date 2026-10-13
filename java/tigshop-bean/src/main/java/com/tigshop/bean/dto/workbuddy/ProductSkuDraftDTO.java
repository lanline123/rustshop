package com.tigshop.bean.dto.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多规格商品的单个规格组合。
 *
 * <p>规格用结构化的名值对表达，而非 Excel 模板里的 {@code 颜色：红色；尺码：37} 文本：
 * 后者是单元格只能存字符串所致的妥协，且分组分隔符有四种写法，对调用方是额外的出错面。
 */
@Data
@Schema(description = "WorkBuddy商品规格草稿")
public class ProductSkuDraftDTO {

    /** 规格名到规格值，如 {@code {"颜色":"红色","尺码":"37"}}；同一批 SKU 的规格名必须一致。 */
    @Schema(description = "规格名值对，如 {\"颜色\":\"红色\",\"尺码\":\"37\"}")
    private Map<String, String> specs = new LinkedHashMap<>();

    @Schema(description = "该规格的销售价")
    @DecimalMin("0.00")
    private BigDecimal price;

    @Schema(description = "该规格的库存")
    private Integer stock;

    @Schema(description = "规格编码，留空时由系统按 商品编码-规格ID 自动生成")
    private String skuSn;

    @Schema(description = "规格条码")
    private String skuTsn;

    @DecimalMin("0.00")
    private BigDecimal marketPrice;

    @DecimalMin("0.00")
    private BigDecimal costPrice;
}
