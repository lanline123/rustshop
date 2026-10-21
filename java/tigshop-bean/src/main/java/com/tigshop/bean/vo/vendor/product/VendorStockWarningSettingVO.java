package com.tigshop.bean.vo.vendor.product;

import lombok.Data;

/** 供应商库存预警通知开关。 */
@Data
public class VendorStockWarningSettingVO {
    private Integer vendorId;
    private Integer notifyEnabled;
    private Integer defaultLowerLimit;
}
