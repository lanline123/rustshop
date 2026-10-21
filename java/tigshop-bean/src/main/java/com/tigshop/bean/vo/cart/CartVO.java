// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.cart;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tigshop.bean.dto.cart.CarExtraSkuDataDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.shop.ShopBaseVO;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 购物车VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车参数")
public class CartVO {

    // *** Cart ***

    @Schema(description = "购物车ID")
    private Integer cartId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    @Schema(description = "原价")
    private String originalPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "购物车类型")
    private Integer type;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "销售员ID")
    private Integer salesmanId;

    @Schema(description = "扩展数据")
    private String extraData;

    @Schema(description = "周期购规格ID（周期购商品时使用）")
    private Integer cycleSpecId;

    @Schema(description = "兑换次卡ID（次卡兑换时使用）")
    private Integer redeemCardId;

    // *** Product ***

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "卡券组ID")
    private Integer cardGroupId;

    @Schema(description = "礼品卡组ID")
    private Integer giftCardGroupsId;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "是否到店自提；0-否，1-是")
    private Integer isShopPickup;

    @Schema(description = "是否门店配送；0-否，1-是")
    private Integer isShopDelivery;

    @Schema(description = "是否物流配送；0-否，1-是")
    private Integer isLogistics;

    // *** ProductSku ***

    @Schema(description = "SKU信息")
    private ProductSkuDTO sku;

    // *** Shop ***

    @Schema(description = "店铺信息")
    private ShopBaseVO shop;

    // *** Other ***

    @Schema(description = "会员价格")
    private BigDecimal memberPrice;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "原始价格")
    private BigDecimal originPrice;

    @Schema(description = "改价前的价格(最开始的price的值)")
    private BigDecimal changePriceBeforePrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "是否选中")
    private Boolean isChecked;

    @Schema(description = "额外SKU数据")
    private List<CarExtraSkuDataDTO> extraSkuData;

    @Schema(description = "额外SKU数据")
    private List<CarExtraSkuDataDTO> extraSkuAllData;

    @Schema(description = "SKU 数据(有可能返回是翻译的数据)")
    private JSONArray skuData;

    @Schema(description = "SKU 数据")
    private JSONArray originSkuData;

    @Schema(description = "是否有SKU")
    private Boolean hasSku;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    @Schema(description = "购物车改价数据")
    private OrderCheckParam.CartChangePrice cartChangePrice;

    // *** Unknown ***

    @Schema(description = "是否禁用")
    private Boolean isDisabled;

    @Schema(description = "禁用原因：product_offline-商品下架，sku_disabled-SKU禁用，sold_out-库存不足")
    private String disabledReason;

    @Schema(description = "活动信息")
    private List<PromotionVO> activityInfo;

    @Schema(description = "服务费")
    private String serviceFee;

    @Schema(description = "是否秒杀")
    private Integer isSeckill;

    @Schema(description = "销售模式：0-现货，1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "是否预售商品")
    private Boolean isPresale;

    @Schema(description = "运费类型")
    private Integer fixedShippingType;

    @Schema(description = "固定运费金额")
    private BigDecimal fixedShippingFee;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "供应商商品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商商品skuID")
    private Integer vendorProductSkuId;

    @Schema(description = "商品限购数量")
    private Integer limitNumber;

    @Schema(description = "开始的商品价格")
    private BigDecimal startProductPrice;

    @Schema(description = "B2B批发：是否批发行（命中批发价时 true）")
    private Boolean isWholesaleLine;

    @Schema(description = "B2B批发：价格模式 1=折扣 2=固定")
    private Integer wholesalePriceMode;

    @Schema(description = "B2B批发：行级提示（阶梯/倍数未达标），null=无")
    private String wholesaleHint;

    @Schema(description = "B2B批发：批发作用域行（批发买家+该商品有批发配置即 true，无论是否命中阶梯价；用于剔除 C 端营销）")
    private Boolean wholesaleScope;

    @Schema(description = "B2B批发：起批数量（仅批发作用域行返回；null/≤1=无起批约束）")
    private Integer wholesaleMoq;

    @Schema(description = "B2B批发：起批维度 1=SPU总量起批 2=SKU单独起批（仅批发作用域行返回）")
    private Integer wholesaleMoqDimension;

    @Schema(description = "B2B批发：倍数购买（仅批发作用域行返回；按 SKU 配置，null/≤1=无倍数约束）")
    private Integer wholesaleMultiple;

    public CartVO(Cart cart, Product product, ProductSku productSku, Shop shop, BigDecimal productPrice, BigDecimal memberPrice, Integer stock) {
        this.cartId = cart.getCartId();
        this.userId = cart.getUserId();
        this.productId = cart.getProductId();
        this.picThumb = cart.getPicThumb();
        this.marketPrice = cart.getMarketPrice();
        this.originalPrice = cart.getOriginalPrice()
                                 .toString();
        this.quantity = cart.getQuantity();
        this.skuId = cart.getSkuId();
        this.shopId = cart.getShopId();
        this.type = cart.getType();
        this.updateTime = cart.getUpdateTime();
        this.salesmanId = cart.getSalesmanId();
        this.extraData = cart.getExtraData();
        this.cycleSpecId = cart.getCycleSpecId();
        this.redeemCardId = cart.getRedeemCardId();

        this.productSn = product.getProductSn();
        this.productName = product.getProductName();
        this.productType = product.getProductType();
        this.brandId = product.getBrandId();
        this.cardGroupId = product.getCardGroupId();
        this.giftCardGroupsId = product.getGiftCardGroupsId();
        this.productStock = product.getProductStock();
        this.categoryId = product.getCategoryId();
        this.suppliersId = product.getSuppliersId();
        this.virtualSample = product.getVirtualSample();
        this.freeShipping = product.getFreeShipping();
        this.shippingTplId = product.getShippingTplId();
        this.productStatus = product.getProductStatus();
        this.noShipping = product.getNoShipping();
        this.productWeight = product.getProductWeight();
        this.isShopPickup = product.getIsShopPickup();
        this.isShopDelivery = product.getIsShopDelivery();
        this.isLogistics = product.getIsLogistics();
        this.fixedShippingType = product.getFixedShippingType();
        this.fixedShippingFee = product.getFixedShippingFee();
        this.saleMode = product.getSaleMode();
        this.isPresale = product.getSaleMode() != null && product.getSaleMode() > 0;

        this.vendorId = product.getVendorId();
        this.vendorProductId = product.getVendorProductId();
        this.vendorProductSkuId = 0;
        if (productSku != null) {
            ProductSkuDTO productSkuDTO = BeanUtil.copyProperties(productSku, ProductSkuDTO.class, "skuData");
            // TODO 此方法有问题
//            List<ProductSkuDTO.SkuData> skuData = JsonUtil.jsonToList(productSku.getSkuData(), ProductSkuDTO.SkuData.class);
           List<ProductSkuDTO.SkuData> skuData =  JsonUtil.fromJson(productSku.getSkuData(), new TypeReference<List<ProductSkuDTO.SkuData>>() {
            });
            productSkuDTO.setSkuData(skuData);
            productSkuDTO.setSkuPrice(productSku.getSkuPrice()
                                                .toString());

            this.sku = productSkuDTO;
            this.vendorProductSkuId = productSku.getVendorProductSkuId();
        }

        if (shop != null) {
            this.shop = BeanUtil.copyProperties(shop, ShopBaseVO.class);
        }




        String changePriceData = cart.getChangePriceData();
        if (StrUtil.isNotBlank(changePriceData) && !"{}".equals(changePriceData)) {
            this.cartChangePrice = JsonUtil.fromJson(changePriceData, OrderCheckParam.CartChangePrice.class);
        }


        this.productPrice = productPrice;
        this.price = productPrice;
        this.originPrice = productPrice;
        // 禁用 SKU 的可售库存查询会按销售安全规则返回 0；列表展示仍返回该 SKU 的真实库存，
        // 是否可操作由 isDisabled/disabledReason 表达。
        this.stock = productSku != null && Objects.equals(productSku.getIsEnabled(), Constants.NO)
                && productSku.getSkuStock() != null ? productSku.getSkuStock() : stock;
        this.extraSkuData = StrUtil.isNotBlank(cart.getExtraSkuData()) ? JSONUtil.parseArray(cart.getExtraSkuData())
                                                                                 .toList(CarExtraSkuDataDTO.class) : null;
        this.isChecked = Constants.YES.equals(cart.getIsChecked());
        this.skuData = JsonUtil.fromJson(cart.getSkuData(), JSONArray.class);
        this.originSkuData = JsonUtil.fromJson(cart.getSkuData(), JSONArray.class);
        this.hasSku = this.sku != null;
        this.subtotal = productPrice.multiply(new BigDecimal(cart.getQuantity()))
                                    .setScale(2, RoundingMode.HALF_UP);
        this.memberPrice = memberPrice;
    }


    public void changePrice(@Nullable OrderCheckParam.CartChangePrice changePrice) {
        if (Objects.isNull(changePrice) || !Boolean.TRUE.equals(this.isChecked)) {
            //没有和没有选中的话,不进行改价
            return;
        }
        Integer productId = changePrice.getProductId();
        if (!Objects.equals(productId, this.productId)) {
            throw new GlobalException("购物车数据有误");
        }
        //改价
        final BigDecimal changePriceValue = changePrice.getChangePriceValue();
        final Integer changePriceType = changePrice.getChangePriceType();
        final BigDecimal lastChangePriceAmount = calculateChangePrice(changePriceValue, changePriceType, this.subtotal);
        //改价前的价格(进行存储)
        this.changePriceBeforePrice = this.price;
        //价格
        this.price = lastChangePriceAmount.divide(new BigDecimal(this.quantity), 2, RoundingMode.HALF_UP);
        //小计(price*数量)
        this.subtotal = lastChangePriceAmount;
    }

    @NotNull
    public static BigDecimal calculateChangePrice(BigDecimal changePriceValue, Integer changePriceType, BigDecimal originPrice) {
        final BigDecimal lastChangePriceAmount;
        if (changePriceType == 1) {
            //一口价
            lastChangePriceAmount = changePriceValue;
        } else if (changePriceType == 2) {
            //折扣
            lastChangePriceAmount = changePriceValue.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                                                    .multiply(originPrice)
                                                    .setScale(2, RoundingMode.HALF_UP);
        } else if (changePriceType == 3) {
            //减少
            lastChangePriceAmount = originPrice.subtract(changePriceValue);
        } else {
            throw new GlobalException("不支持该改价类型");
        }
        return lastChangePriceAmount.setScale(2, RoundingMode.HALF_UP);
    }
}
