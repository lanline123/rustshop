package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 供应商库存预警通知总开关。 */
@Data
@TableName("vendor_stock_warning_setting")
public class VendorStockWarningSetting implements Serializable {
    @TableId(value = "vendor_id", type = IdType.INPUT)
    private Integer vendorId;
    private Integer notifyEnabled;
    private Long createTime;
    private Long updateTime;
}
