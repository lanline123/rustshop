package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存预警判定所需的商品快照
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警判定行")
public class StockWarningEvaluateRowVO {

    private Integer productId;

    private String productName;

    /** 规格值原文，消息文案中展示前需翻译 */
    private String skuValue;

    private Integer vendorId;

    /** 按主体口径计算的当前库存 */
    private Integer currentStock;
}
