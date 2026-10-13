package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店预约上门服务距离溢价表
 *
 * @TableName shop_home_service_distance_pricing
 */
@TableName(value = "shop_home_service_distance_pricing")
@Data
public class ShopHomeServiceDistancePricing {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer shopHomeServiceDistancePricingId;

    /**
     * 预约上门服务配置ID，关联 shop_home_service_config.shop_home_service_config_id
     */
    private Integer shopHomeServiceConfigId;

    /**
     * 超过多少公里
     */
    private BigDecimal distanceThreshold;

    /**
     * 每增加多少公里
     */
    private BigDecimal distanceUnit;

    /**
     * 上门费增加多少元
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
