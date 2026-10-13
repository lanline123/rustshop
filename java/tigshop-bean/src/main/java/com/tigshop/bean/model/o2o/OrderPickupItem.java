package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 13:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("order_pickup_item")
@Builder
public class OrderPickupItem {
    
    @TableId(value = "order_pickup_id", type = IdType.AUTO)
    private Integer orderPickupId;
    
    @TableField("shop_id")
    private Integer shopId;
    
    @TableField("pickup_id")
    private Integer pickupId;
    
    @TableField("order_id")
    private Integer orderId;

    @TableField("expect_pickup_time")
    private String expectPickupTime;

    @TableField("user_pickup_id")
    private Integer userPickupId;
}
