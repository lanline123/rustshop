package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批发价详情 VO
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发价详情")
public class WholesalePriceDetailVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "SPU 是否多 SKU")
    private Boolean isMultiSpec;

    @Schema(description = "多规格价格模式：1=商品级混批 2=SKU级；单规格忽略")
    private Integer multiSpecPriceMode;

    @Schema(description = "该商品当前主要 mode（仅显示用；行级 mode 以 price 行为准）")
    private Integer priceMode;

    @Schema(description = "SKU 批发价列表")
    private List<WholesaleSkuPriceVO> skus;
}
