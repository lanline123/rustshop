package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店同城配送配置表
 * @TableName shop_local_delivery_config
 */
@TableName(value ="shop_local_delivery_config")
@Data
public class ShopLocalDeliveryConfig {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer shopLocalDeliveryConfigId;

    /**
     * 门店ID
     */
    private Integer shopId;

    /**
     * 是否开启同城配送 0=关闭 1=开启
     */
    private Integer enabled;

    /**
     * 配送半径（公里）
     */
    private BigDecimal deliveryRadius;

    /**
     * 起送价（元）
     */
    private BigDecimal minOrderAmount;

    /**
     * 基础运费（元）
     */
    private BigDecimal baseDeliveryFee;

    /**
     * 是否开启叠加溢价 0=关闭 1=开启
     */
    private Integer distancePricingEnabled;

    /**
     * 派单模式：1-抢单模式，2-手动派单
     */
    private Integer orderDispatchMode;

    /**
     * 是否开启取货时拍照 0=关闭 1=开启
     */
    private Integer pickupPhotoEnabled;

    /**
     * 是否开启配送完成拍照 0=关闭 1=开启
     */
    private Integer deliveryCompletePhotoEnabled;

    /**
     * 添加时间
     */
    private Integer addTime;
}
