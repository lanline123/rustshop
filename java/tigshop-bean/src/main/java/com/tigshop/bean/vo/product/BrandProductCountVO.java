package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "品牌及商品数量VO")
@Data
public class BrandProductCountVO {

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "品牌下商品数量（实时统计）")
    private Integer productCount;
}
