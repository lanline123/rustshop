package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品预约时间段配置
 * @TableName product_reservation_time
 */
@TableName(value ="product_reservation_time")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReservationTime {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer productReservationTimeId;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 开始时间，格式HH:mm
     */
    private String startTime;

    /**
     * 结束时间，格式HH:mm
     */
    private String endTime;

    /**
     * 添加时间 
     */
    private Long addTime;
}