package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 商品会员价格
 *
 * @TableName product_member_price
 */
@TableName(value = "product_member_price")
@Data
public class ProductMemberPrice implements Serializable {
    /**
     * 折扣价自增id
     */
    @TableId(type = IdType.AUTO)
    private Integer priceId;

    /**
     * 商品的id
     */
    private Integer productId;

    /**
     * 会员等级id
     */
    private Integer userRank;

    /**
     * 指定商品对指定会员等级的固定定价价格，单位元
     */
    private BigDecimal userPrice;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}