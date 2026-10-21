package com.tigshop.bean.dto.o2o.stockcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "同一设置库存参数")
public class UnifiedStockSetDTO {
    
    /**
     * 商品ID
     */
    @Schema(description = "商品ID", required = true)
    private Integer productId;

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID", required = true)
    private Integer skuId;
    
    /**
     * 门店选择配置
     */
    @Schema(description = "门店选择配置", required = true)
    private ShopSelectionDTO shopSelection;
    
    /**
     * 开始日期
     */
    @Schema(description = "开始日期，格式：yyyy-MM-dd", required = true)
    private String startDate;
    
    /**
     * 结束日期
     */
    @Schema(description = "结束日期，格式：yyyy-MM-dd", required = true)
    private String endDate;
    
    /**
     * 日期排除配置
     */
    @Schema(description = "日期排除配置")
    private DateExclusionDTO dateExclusion;
    
    /**
     * 数量变化（正数为增加，负数为减少）
     */
    @Schema(description = "数量变化（正数为增加，负数为减少）", required = true)
    private Integer quantity;
}