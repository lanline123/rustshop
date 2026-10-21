package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 产品预约日历库存
 * @TableName product_reservation_stock
 */
@TableName(value ="product_reservation_stock")
@Data
public class ProductReservationStock {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long productReservationStockId;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 门店ID
     */
    private Integer shopId;

    /**
     * SKU ID
     */
    private Integer skuId;

    /**
     * 预约时间段ID，对应 product_reservation_time.id
     */
    private Integer productReservationTimeId;

    /**
     * 库存日期，比如 2025-09-18
     */
    private String stockDate;

    /**
     * 总库存数
     */
    private Integer totalStock;

    /**
     * 已用库存数
     */
    private Integer usedStock;

    /**
     * 添加时间 
     */
    private Integer addTime;
}