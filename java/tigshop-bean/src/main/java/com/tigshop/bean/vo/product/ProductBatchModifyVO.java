package com.tigshop.bean.vo.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品导入 响应
 *
 * @author kidd
 * @since 2025/3/27 10:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchModifyVO {

    private Integer count;

    private String msg;
}
