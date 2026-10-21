package com.tigshop.bean.vo.product;

import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品能力返回VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商品能力返回VO")
public class ProductAvailabilityVO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "数据")
    private List<ProductSkuDTO.SkuData> data;

    @Schema(description = "SKU信息")
    private ProductSkuDTO sku;

    @Schema(description = "实时计算的定金金额（仅定金预售返回）")
    private BigDecimal depositAmount;

    @Schema(description = "尾款金额（price-depositAmount）")
    private BigDecimal finalPaymentAmount;

    @Schema(description = "原价")
    private BigDecimal originPrice;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    private BigDecimal startProductPrice;

    @Schema(description = "价格")
    private BigDecimal price;


    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "促销信息")
    private List<PromotionVO> promotion;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "距离")
    private BigDecimal distance;

    @Schema(description = "礼品卡组id")
    private Integer giftCardGroupsId;

}
