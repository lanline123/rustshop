package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 秒杀商品列表
 *
 * @author kidd
 * @since 2025/4/16 16:40
 */
@Data
public class SeckillPromotionVO {

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "产品TSN")
    private String productTsn;

    @Schema(description = "市场价（划线价）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "图片URL")
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "审核原因")
    private String checkReason;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "产品类型")
    private Integer productType;

    @Schema(description = "产品SN")
    private String productSn;

    @Schema(description = "产品价格")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

    @Schema(description = "原始商品价格")
    private String orgProductPrice;

    @Schema(description = "产品状态")
    private Integer productStatus;

    @Schema(description = "是否推荐")
    private Integer isBest;

    @Schema(description = "是否新品")
    private Integer isNew;

    @Schema(description = "是否热销")
    private Integer isHot;

    @Schema(description = "库存数量")
    private Integer productStock;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "产品SKU列表")
    private List<ProductSkuDTO> productSku;

    @Schema(description = "店铺信息")
    private Shop shop;

    @Schema(description = "秒杀活动限购数量")
    private Integer seckillLimitNum;

    @Schema(description = "秒杀活动销量")
    private Integer seckillSales;

    @Schema(description = "秒杀活动库存")
    private Integer seckillStock;

    @Schema(description = "秒杀活动信息")
    private SeckillVO seckkillData;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "货品编码")
    private String skuSn;

}
