package com.tigshop.bean.vo.finance.refundapply;

import cn.hutool.json.JSONArray;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.common.utils.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 退款申请详情订单项
 *
 * @author kidd
 * @since 2025/4/27 13:47
 */
@Schema(description = "退款申请详情订单项")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundApplyDetailOrderItemVO {

    // *** OrderItem ***

    @Schema(description = "订单商品信息自增id")
    private Integer itemId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "商品最终单价")
    private BigDecimal price;

    @Schema(description = "商品的购买数量")
    private Integer quantity;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品的名称")
    private String productName;

    @Schema(description = "商品的唯一货号")
    private String productSn;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "sku信息（JSON）[{sku_id,sku_value,sku_name}]")
    private String skuData;

    @Schema(description = "发货数量")
    private Integer deliveryQuantity;

    @Schema(description = "商品类型，1：实体，2：虚拟，3赠品")
    private Integer productType;

    @Schema(description = "是否为赠品")
    private Integer isGift;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "是否拼团")
    private Integer isPin;

    @Schema(description = "预售价格")
    private BigDecimal prepayPrice;

    @Schema(description = "佣金数据")
    private String commission;

    @Schema(description = "商品初始价格")
    private BigDecimal originPrice;

    @Schema(description = "是否秒杀")
    private Integer isSeckill;

    @Schema(description = "额外SKU数据")
    private String extraSkuData;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    // *** Product ***

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "付费内容")
    private JSONArray paidContent;

    @Schema(description = "卡组ID")
    private Integer cardGroupId;

    // *** ProductSku ***

    @Schema(description = "货品库存")
    private Integer skuStock;

    @Schema(description = "货品编码")
    private String skuSn;

    @Schema(description = "商品规格值")
    private String skuValue;

    // *** ECard ***

    @Schema(description = "电子卡券")
    private List<ECard> eCards;

    @Schema(description = "退货数量")
    private Integer number;

    @Schema(description = "税率(百分比)")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRate;

    @Schema(description = "单个商品税费（按商品单价和税率计算，保留2位小数并向上取整）")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRateAmount;

    @Schema(description = "该项税费合计（taxRateAmount * 退款数量）")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxTotalAmount;

    public RefundApplyDetailOrderItemVO(OrderItem orderItem, List<AftersalesItem> aftersalesItems, List<Product> products, List<ProductSku> productSkus, List<ECard> eCards, BigDecimal orderTaxRate) {
        this.itemId = orderItem.getItemId();
        this.orderId = orderItem.getOrderId();
        this.orderSn = orderItem.getOrderSn();
        this.userId = orderItem.getUserId();
        this.price = orderItem.getPrice();
        this.quantity = orderItem.getQuantity();
        this.productId = orderItem.getProductId();
        this.productName = orderItem.getProductName();
        this.productSn = orderItem.getProductSn();
        this.skuId = orderItem.getSkuId();
        this.skuData = orderItem.getSkuData();
        this.deliveryQuantity = orderItem.getDeliveryQuantity();
        this.productType = orderItem.getProductType();
        this.isGift = orderItem.getIsGift();
        this.shopId = orderItem.getShopId();
        this.isPin = orderItem.getIsPin();
        this.prepayPrice = orderItem.getPrepayPrice();
        this.commission = orderItem.getCommission();
        this.originPrice = orderItem.getOriginPrice();
        this.isSeckill = orderItem.getIsSeckill();
        this.extraSkuData = orderItem.getExtraSkuData();
        this.suppliersId = orderItem.getSuppliersId();

        products.stream()
                .filter(product -> product.getProductId().equals(orderItem.getProductId()))
                .findFirst()
                .ifPresent(product -> {
                    this.picThumb = product.getPicThumb();
                    this.productStock = product.getProductStock();
                    this.productWeight = product.getProductWeight();
                    this.virtualSample = product.getVirtualSample();
                    this.paidContent = JsonUtil.checkJsonType(product.getPaidContent());
                    this.cardGroupId = product.getCardGroupId();
                });

        productSkus.stream()
                .filter(productSku -> productSku.getSkuId().equals(orderItem.getSkuId()))
                .findFirst()
                .ifPresent(productSku -> {
                    this.skuStock = productSku.getSkuStock();
                    this.skuSn = productSku.getSkuSn();
                    this.skuValue = productSku.getSkuValue();
                });

        this.eCards = eCards;

        aftersalesItems.stream()
                .filter(aftersalesItem -> aftersalesItem.getOrderItemId().equals(orderItem.getItemId()))
                .findFirst()
                .ifPresent(aftersalesItem -> {
                    this.number = aftersalesItem.getNumber();
                });

        this.taxRate = orderTaxRate != null ? orderTaxRate : BigDecimal.ZERO;
        this.taxRateAmount = orderItem.getPrice() != null
                && orderItem.getPrice().compareTo(BigDecimal.ZERO) > 0
                && this.taxRate.compareTo(BigDecimal.ZERO) > 0
                ? orderItem.getPrice().multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.CEILING)
                : BigDecimal.ZERO;
        // price 回显为“商品单价 + 单件税费”，便于部分退款按含税单价计算
        this.price = (orderItem.getPrice() != null ? orderItem.getPrice() : BigDecimal.ZERO).add(this.taxRateAmount);
        Integer taxQuantity = this.number != null ? this.number : this.quantity;
        this.taxTotalAmount = taxQuantity != null
                ? this.taxRateAmount.multiply(BigDecimal.valueOf(taxQuantity))
                : BigDecimal.ZERO;
    }

}
