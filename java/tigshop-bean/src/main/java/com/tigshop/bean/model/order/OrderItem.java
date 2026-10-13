// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单商品model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("order_item")
@Schema(description = "订单商品")
public class OrderItem implements Serializable {

    @Schema(description = "订单商品信息自增id")
    @TableId(type = IdType.AUTO)
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

    @Schema(description = "改价前单位价")
    private BigDecimal adminChangePrePrice;

    @Schema(description = "优惠信息")
    private String promotionData;

    @Schema(description = "是否秒杀")
    private Integer isSeckill;

    @Schema(description = "额外SKU数据")
    private String extraSkuData;

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

    @Schema(description = "改价数据")
    private String changePriceData;

    @Schema(description = "礼品卡ID")
    private Integer giftCardId;

    @Schema(description = "配送员服务费（单件快照，仅 O2O 版本写入）")
    private BigDecimal deliveryServiceFee;

    @Schema(description = "服务员服务费（单件快照，仅 O2O 上门服务订单写入）")
    private BigDecimal serverServiceFee;

    @Schema(description = "B2B批发：1=批发行 0=零售行")
    private Integer isWholesaleLine;

    @Schema(description = "B2B批发：等级快照(0=无)")
    private Integer wholesaleGradeId;

    @Schema(description = "B2B批发：批发基准价快照(冻结,审计/退款用,design §6)")
    private BigDecimal wholesaleBasePrice;

}