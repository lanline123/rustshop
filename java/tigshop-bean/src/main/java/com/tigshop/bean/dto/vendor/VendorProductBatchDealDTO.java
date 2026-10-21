package com.tigshop.bean.dto.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商商品导出Excel
 */
@Data
public class VendorProductBatchDealDTO {

    @Schema(description = "数据类型；1-分类，2-品牌，3-供应商商品, 4-不包含供应商商品")
    private Integer dealRange;

    @Schema(description = "数据主键")
    private String rangeIds;
}
