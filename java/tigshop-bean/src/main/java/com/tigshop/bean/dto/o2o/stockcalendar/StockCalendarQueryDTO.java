package com.tigshop.bean.dto.o2o.stockcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商品时间段库存查看参数")
public class StockCalendarQueryDTO {
    
    /**
     * 商品ID
     */
    @Schema(description = "商品ID", required = true)
    private Integer productId;
    
    /**
     * 门店ID
     */
    @Schema(description = "门店ID", required = true)
    private Integer shopId;

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID")
    private Integer skuId;

}