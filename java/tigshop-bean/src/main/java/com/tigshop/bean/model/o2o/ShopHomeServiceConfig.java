package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店预约上门服务配置表
 *
 * @TableName shop_home_service_config
 */
@TableName(value = "shop_home_service_config")
@Data
public class ShopHomeServiceConfig {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer shopHomeServiceConfigId;

    /**
     * 门店ID
     */
    private Integer shopId;

    /**
     * 是否开启预约上门服务 0=关闭 1=开启
     */
    private Integer enabled;

    /**
     * 服务半径（米）
     */
    private BigDecimal serviceRadius;

    /**
     * 上门费（元）
     */
    private BigDecimal homeFee;

    /**
     * 是否开启距离溢价 0=关闭 1=开启
     */
    private Integer distancePricingEnabled;

    /**
     * 派单模式：1-抢单模式，2-手动派单
     */
    private Integer orderDispatchMode;

    /**
     * 是否开启服务前拍照 0=关闭 1=开启
     */
    private Integer beforeServicePhotoEnabled;

    /**
     * 是否开启服务后拍照 0=关闭 1=开启
     */
    private Integer afterServicePhotoEnabled;

    /**
     * 添加时间
     */
    private Integer addTime;
}
