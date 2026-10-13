package com.tigshop.bean.vo.vendor.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商商品导入 响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductBatchModifyVO {

    private Integer count;

    private String msg;
}
