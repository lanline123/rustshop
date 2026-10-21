package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 门店自提模板表
 * @TableName shop_pickup_tpl
 */
@TableName(value ="shop_pickup_tpl")
@Data
public class ShopPickupTpl {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long shopPickupTplId;

    /**
     * 门店ID，关联shop表
     */
    private Long shopId;

    /**
     * 模板名称
     */
    private String tplName;

    /**
     * 完成备货状态：0-禁用，1-启用
     */
    private Integer stockingStatus;

    /**
     * 完成备货JSON
     */
    private String stockingJson;

    /**
     * 自提时间状态：0-禁用，1-启用
     */
    private Integer pickupTimeStatus;

    /**
     * 自提时间JSON
     */
    private String pickupTimeJson;

    /**
     * 提货有效期状态：0-禁用，1-启用
     */
    private Integer pickupEndStatus;

    /**
     * 提货有效期JSON
     */
    private String pickupEndJson;

    /**
     * 是否默认
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}