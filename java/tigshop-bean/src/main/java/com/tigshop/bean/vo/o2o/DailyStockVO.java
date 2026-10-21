package com.tigshop.bean.vo.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "某日期库存信息返回结果")
public class DailyStockVO {
    
    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Integer productId;
    
    /**
     * 门店ID
     */
    @Schema(description = "门店ID")
    private Integer shopId;

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "规格值")
    private String specValue;
    
    /**
     * 查询日期
     */
    @Schema(description = "查询日期，格式：yyyy-MM-dd")
    private String stockDate;
    
    /**
     * 时间段库存列表
     */
    @Schema(description = "时间段库存列表")
    private List<TimeSlotStockInfo> timeSlotStockList;
    
    @Data
    @Schema(description = "时间段库存信息")
    public static class TimeSlotStockInfo {
        
        /**
         * 产品预约时间ID
         */
        @Schema(description = "产品预约时间ID")
        private Integer productReservationTimeId;
        
        /**
         * 开始时间
         */
        @Schema(description = "开始时间，格式：HH:mm")
        private String startTime;
        
        /**
         * 结束时间
         */
        @Schema(description = "结束时间，格式：HH:mm")
        private String endTime;
        
        /**
         * 总库存数
         */
        @Schema(description = "总库存数")
        private Integer totalStock;
        
        /**
         * 已用库存数
         */
        @Schema(description = "已用库存数")
        private Integer usedStock;
        
        /**
         * 可用库存数
         */
        @Schema(description = "可用库存数")
        private Integer availableStock;
    }
}