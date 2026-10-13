package com.tigshop.bean.vo.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "库存日历查看结果")
public class StockCalendarVO {
    
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

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID")
    private Integer skuId;
    
    /**
     * 日期库存列表
     */
    @Schema(description = "日期库存列表")
    private List<DateStockInfo> dateStockList;
    
    @Data
    @Schema(description = "日期库存信息")
    public static class DateStockInfo {
        
        /**
         * 日期
         */
        @Schema(description = "日期，格式：yyyy-MM-dd")
        private String stockDate;
        
        /**
         * 时间段库存列表
         */
        @Schema(description = "时间段库存列表")
        private List<TimeSlotStockInfo> timeSlotStockList;
    }
    
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

        /**
         * 是否可用
         */
        @Schema(description = "是否可用 0 不能选择，1 可以选择")
        private Integer enable;
    }
}