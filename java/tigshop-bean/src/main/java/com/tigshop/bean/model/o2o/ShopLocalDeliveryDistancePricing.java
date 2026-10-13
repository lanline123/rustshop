package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店同城配送距离叠加溢价表
 * @TableName shop_local_delivery_distance_pricing
 */
@TableName(value ="shop_local_delivery_distance_pricing")
@Data
public class ShopLocalDeliveryDistancePricing {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer shopLocalDeliveryDistancePricingId;

    /**
     * 门店配送配置ID，关联 shop_local_delivery_config.shop_local_delivery_config_id
     */
    private Integer shopLocalDeliveryConfigId;

    /**
     * 超过多少公里
     */
    private BigDecimal distanceThreshold;

    /**
     * 每增加多少公里
     */
    private BigDecimal distanceUnit;

    /**
     * 配送增加多少元
     */
    private BigDecimal additionalFee;

    /**
     * 排序，数值越小越优先
     */
    private Integer sortOrder;

    /**
     * 添加时间
     */
    private Integer addTime;
}