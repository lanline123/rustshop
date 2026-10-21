package com.tigshop.bean.vo.vendor.product;

import lombok.Data;

/** 供应商库存预警列表项。 */
@Data
public class VendorStockWarningPageVO {
    private Integer productId;
    private Integer skuId;
    private String productName;
    private String productSkuName;
    private String productPicThumb;
    private String productSn;
    private String skuSn;
    private String productTsn;
    private Integer vendorId;
    private Integer currentStock;
    private Integer lowerLimit;
    private Integer upperLimit;
    private Integer stockStatus;
    private String stockStatusName;
    private Integer warnEnabled;
    private Long updateTime;
}
