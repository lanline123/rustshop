package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺-会员绑定表。
 *
 * <p>开关开启后，用户在某店铺/门店下单支付即成为该店会员，按 (shopId, userId) 唯一。
 * order_amount / order_count 按「已支付」累加。
 */
@Data
@TableName("shop_member")
@Schema(description = "店铺会员绑定")
public class ShopMember implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "店铺ID（门店/店铺统一）")
    private Integer shopId;

    @Schema(description = "该店累计已支付订单数")
    private Integer orderCount;

    @Schema(description = "该店累计消费金额（已支付）")
    private BigDecimal orderAmount;

    @Schema(description = "入会时间（首次成为会员，Unix秒）")
    private Long joinTime;

    @Schema(description = "最近消费时间（最近一次已支付，Unix秒）")
    private Long lastConsumeTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @TableLogic
    @Schema(description = "逻辑删除；0-否，1-是")
    private Integer isDel;

    @Serial
    private static final long serialVersionUID = 1L;
}
