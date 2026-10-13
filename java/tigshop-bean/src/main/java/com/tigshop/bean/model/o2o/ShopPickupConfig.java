package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 门店自提配置表
 * @TableName shop_pickup_config
 */
@TableName(value ="shop_pickup_config")
@Data
public class ShopPickupConfig {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long shopPickupConfigId;

    /**
     * 门店ID，关联shop表
     */
    private Long shopId;

    /**
     * 自定义名称（该名称会显示在买家可选择的配送方式中，默认名称"到店自提"）
     */
    private String pickupName;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 提货字段设置
     */
    private String pickupFiled;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}