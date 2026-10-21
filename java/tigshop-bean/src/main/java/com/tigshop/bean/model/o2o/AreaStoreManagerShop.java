package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 区域门店绑定门店表
 * @TableName area_store_manager_shop
 */
@TableName(value ="area_store_manager_shop")
@Data
public class AreaStoreManagerShop {
    /**
     * 区域门店绑定id
     */
    @TableId(type = IdType.AUTO)
    private Integer areaStoreManagerShopId;

    /**
     * 区域门店ID
     */
    private Integer areaStoreManagerId;

    /**
     * 门店ID
     */
    private Integer shopId;

    /**
     * 创建时间
     */
    private Long createTime;
}