package com.tigshop.bean.vo.wholesale;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商品列表 VO
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商品列表项")
public class WholesaleProductVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "货号")
    private String productSn;

    @Schema(description = "商品编号")
    private String productNo;

    @Schema(description = "商品主图 URL（自动 CDN 前缀拼接）")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "商品缩略图 URL（列表场景推荐使用）")
    private String picThumb;

    @Schema(description = "零售价范围（已格式化，如 ¥10.00-¥99.00）")
    private String retailPriceRange;

    @Schema(description = "批发价范围（已格式化，折扣或金额，如 4.00折-8.00折）")
    private String wholesalePriceDisplay;

    @Schema(description = "起批维度：1=SPU 2=SKU")
    private Integer moqDimension;

    @Schema(description = "起批维度名（派生）")
    private String moqDimensionName;

    @Schema(description = "起批量展示：SPU 维度=moqSpu；SKU 维度=各 SKU min 值")
    private Integer moqDisplay;

    @Schema(description = "是否设置倍数购买（任意 SKU multiple>1）")
    private Boolean multipleSet;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "上架状态（复用 product.shelf_status）")
    private Integer shelfStatus;

    @Schema(description = "批发能力启停（product_wholesale_config.status）")
    private Integer configStatus;
}
