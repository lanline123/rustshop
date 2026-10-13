package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 供应商店铺绑定表
 * @TableName vendor_shop_bind
 */
@TableName(value ="vendor_shop_bind")
@Data
public class VendorShopBind {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 供应商ID
     */
    private Integer vendorId;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 创建时间
     */
    private Long addTime;
}