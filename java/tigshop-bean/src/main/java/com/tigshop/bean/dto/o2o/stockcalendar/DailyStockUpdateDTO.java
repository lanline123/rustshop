package com.tigshop.bean.dto.o2o.stockcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Data
@Schema(description = "修改某日期库存参数")
public class DailyStockUpdateDTO {
    
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
     * 修改日期
     */
    @Schema(description = "修改日期，格式：yyyy-MM-dd", required = true)
    @NotBlank(message = "修改日期不能为空")
    private String stockDate;
    
    /**
     * 时间段库存变更列表
     */
    @Schema(description = "时间段库存变更列表", required = true)
    @NotNull(message = "时间段库存变更列表不能为空")
    @Valid
    private List<TimeSlotStockChange> timeSlotStockChanges;
    
    @Data
    @Schema(description = "时间段库存变更项")
    public static class TimeSlotStockChange {
        
        /**
         * 预约时间段ID
         */
        @Schema(description = "预约时间段ID", required = true)
        @NotNull(message = "预约时间段ID不能为空")
        private Integer productReservationTimeId;
        
        /**
         * 库存变化量（正数为增加，负数为减少）
         */
        @Schema(description = "库存变化量（正数为增加，负数为减少）", required = true)
        @NotNull(message = "库存变化量不能为空")
        private Integer stockChange;
    }
}