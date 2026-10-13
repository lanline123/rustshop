package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品导出Excel
 *
 * @author kidd
 * @since 2025/3/26 17:37
 */
@Data
public class ProductBatchDealDTO {

    @Schema(description = "数据类型；1-分类，2-品牌，3-商品, 4-不包含商品")
    private Integer dealRange;

    private Integer dealType;

    @Schema(description = "数据主键")
    private String rangeIds;
}
