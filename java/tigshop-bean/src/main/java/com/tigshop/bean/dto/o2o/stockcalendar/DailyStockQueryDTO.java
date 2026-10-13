package com.tigshop.bean.dto.o2o.stockcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "查询某日期库存信息参数")
public class DailyStockQueryDTO {
    
    /**
     * 商品ID
     */
    @Schema(description = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Integer productId;
    
    /**
     * 门店ID
     */
    @Schema(description = "门店ID", required = true)
    @NotNull(message = "门店ID不能为空")
    private Integer shopId;

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID", required = true)
    @NotNull(message = "SKU ID不能为空")
    private Integer skuId;
    
    /**
     * 查询日期
     */
    @Schema(description = "查询日期，格式：yyyy-MM-dd", required = true)
    @NotBlank(message = "查询日期不能为空")
    private String stockDate;
}