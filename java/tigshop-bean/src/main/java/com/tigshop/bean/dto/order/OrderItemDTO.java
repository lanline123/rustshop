// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.order;

import com.tigshop.bean.dto.product.ProductSkuDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单商品model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单商品")
public class OrderItemDTO implements Serializable {

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

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "sku信息（JSON）[{sku_id,sku_value,sku_name}]")
    private List<ProductSkuDTO.SkuData> skuData;

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

    @Schema(description = "优惠信息")
    private String promotionData;

    @Schema(description = "是否秒杀")
    private Integer isSeckill;

    @Schema(description = "额外SKU数据")
    private List<Object> extraSkuData;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "卡券组名称")
    private String cardGroupName;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "供应商产品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商产品skuID")
    private Integer vendorProductSkuId;

    @Schema(description = "供货价")
    private BigDecimal vendorProductSupplyPrice;

}