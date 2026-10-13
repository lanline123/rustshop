package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 供应商商品库存预警规则。 */
@Data
@TableName("vendor_stock_warning_rule")
public class VendorStockWarningRule implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer vendorId;
    private Integer productId;
    private Integer skuId;
    private Integer warnEnabled;
    private Integer lowerLimit;
    private Integer upperLimit;
    private Integer lastWarnStatus;
    private Long lastWarnTime;
    @TableLogic
    private Integer isDel;
    private Long createTime;
    private Long updateTime;
}
