// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 商品列表返回参数
 *
 * @author Jayce
 * @create 2024年11月20日 14:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResDTO {
    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "产品TSN")
    private String productTsn;

    @Schema(description = "市场价（划线价）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal marketPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "图片URL")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "大图")
    @JsonTranslate(dataType = 7)
    private String picLarge;

    @Schema(description = "缩略图URL")
    @JsonTranslate(dataType = 7)
    private String picThumb;

    @Schema(description = "商品简介")
    @JsonTranslate(dataType = 1)
    private String productBrief;

    @Schema(description = "产品名称")
    @JsonTranslate(dataType = 2)
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

    @Schema(description = "销售模式：0-现货销售，1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "销售模式：0-现货销售，1-预售 ")
    private Integer productSaleMode;

    @Schema(description = "商品服务")
    private List<Integer> productServiceIds;

    @Schema(description = "产品SN")
    private String productSn;

    @Schema(description = "产品价格")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

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

    @Schema(description = "商品SKU总数（前端据此判断是否走分页编辑；单规格通常为0）")
    private Integer skuCount;

    @Schema(description = "店铺信息")
    private Shop shop;

    @Schema(description = "用户当前位置到当前返回门店的距离，单位：米；仅最近门店搜索场景返回")
    private BigDecimal distance;

    @Schema(description = "是否限时折扣")
    Boolean isTimeDiscount;

    @Schema(description = "最终价")
    private BigDecimal price;

    @Schema(description = "供应商产品ID")
    private Integer vendorProductId;

    @Schema(description = "原始商品价格")
    private String orgProductPrice;

    @Schema(description = "秒杀商品标识")
    private Boolean isSeckill;

    @Schema(description = "是否拼团")
    private Boolean isGroupon;

    @Schema(description = "是否需要重新导入供应商商品")
    private Integer isNeedReImportVendorProduct;

    @Schema(description = "属性模板ID")
    private Integer attributesTplId;

    @Schema(description = "限购数量")
    private Integer limitNumber;

    @Schema(description = "交付模式：1-单次交付，2-多次交付（周期购）")
    private Integer deliveryMode;

    @Schema(description = "礼品卡组类型：0-非礼品卡，1-储值卡，2-兑换卡")
    private Integer groupType;

    @Schema(description = "是否周期购商品")
    private Boolean isCycleProduct;

    @Schema(description = "周期购价格（周期购商品时返回，取第一个周期规格的价格）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal cyclePrice;

    @Schema(description = "周期购原价（划线价）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal cycleOriginalPrice;

    @Schema(description = "周期购规格名称（如：12期）")
    private String cyclePeriodName;

    @Schema(description = "虚拟销量")
    private Integer virtualSales;

    @Schema(description = "交付模式类型：1-按计划配送，2-按需兑换（次卡）；仅 deliveryMode=2 时有值")
    private Integer deliveryType;

    @Schema(description = "是否次卡商品")
    private Boolean isCardProduct;

    @Schema(description = "兑换总次数（次卡模式）")
    private Integer redeemTotalTimes;

    @Schema(description = "生效时间描述，如「付款后立即生效」")
    private String effectiveTypeDesc;

    @Schema(description = "有效期描述，如「长期有效」「生效时间起30天内有效」")
    private String validityDesc;

    @Schema(description = "折算单价（次卡模式 = cyclePrice / redeemTotalTimes）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal unitPrice;

    @Schema(description = "首条批发价（批发商身份命中且商品已配置批发时返回；非批发商或未命中为 null）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal firstWholesalePrice;

    @Schema(description = "卡片可直接加购的起批数（CR-20260603-1720 / CD-035）；4 条命中规则全 true 才返回数字，否则 null；前端用 null 判定跳详情")
    private Integer wholesaleCardMoq;

    @Schema(description = "起批维度（CR-20260613-0943）：1=SPU 总量起批 / 2=SKU 单独起批；与 wholesaleCardMoq 同范围回填，否则 null")
    private Integer wholesaleMoqDimension;

    @Schema(description = "倍数购买（CR-20260613-0943）：取 SPU 兜底 sku_id=0 的 multiple；null/≤1 表示无倍数约束")
    private Integer wholesaleMultiple;

    public ProductListResDTO(Product product, List<ProductSku> productSkus) {
        BeanUtil.copyProperties(product, this);
        this.productSku = productSkus.stream().map(ProductSkuDTO::new).toList();
    }
}
