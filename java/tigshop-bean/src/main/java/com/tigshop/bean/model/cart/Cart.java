package com.tigshop.bean.model.cart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车购物信息记录表
 *
 * @author Tigshop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "cart")
public class Cart implements Serializable {

    @Schema(description = "自增id号")
    @TableId(type = IdType.AUTO)
    private Integer cartId;

    @Schema(description = "用户登录ID;取自session")
    private Integer userId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "货号")
    private String productSn;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    @Schema(description = "添加时的价格")
    private BigDecimal originalPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "【JSON】sku信息[{sku_id,sku_value,sku_name}]")
    private String skuData;

    @Schema(description = "【JSON对象]")
    private String changePriceData;

    @Schema(description = "商品类型，1：实体，2：虚拟")
    private Integer productType;

    @Schema(description = "是否选中")
    private Integer isChecked;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "类型：1：普通购物车商品，2：拼团,3积分兑换4赠品，5砍一砍 6 虚拟 7 付费商品 8 卡密商品 15 周期购商品")
    private Integer type;

    @Schema(description = "购物车商品更新时间")
    private Long updateTime;

    @Schema(description = "周期购规格ID（周期购商品时使用）")
    private Integer cycleSpecId;

    @Schema(description = "兑换次卡ID（次卡兑换时使用，非空即兑换模式）")
    private Integer redeemCardId;

    @Schema(description = "分销员")
    private Integer salesmanId;

    @Schema(description = "【JSON】附加属性信息[{attr_id,attr_value,attr_name}]")
    private String extraSkuData;

    @Schema(description = "扩展数据")
    private String extraData;

    @Schema(description = "起送价（元）")
    @TableField(exist = false)
    private BigDecimal minOrderAmount;

    @Schema(description = "B2B批发：1=批发行 0=零售行")
    private Integer isWholesaleLine;

    @Schema(description = "B2B批发：加车时等级快照(0=无)")
    private Integer wholesaleGradeId;

}