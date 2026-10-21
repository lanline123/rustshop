package com.tigshop.bean.vo.o2o;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "某商品有哪些时段")
public class ProductTimeListVO {
    
    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "图片URL")
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "原图URL")
    private String picOriginal;
    
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

    }
}