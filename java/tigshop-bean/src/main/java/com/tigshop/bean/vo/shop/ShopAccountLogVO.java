package com.tigshop.bean.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺资金变化
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺资金变化")
public class ShopAccountLogVO {
    @Schema(description = "店铺资金变化ID")
    private Integer shopAccountLogId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "店铺资金")
    private BigDecimal shopMoney;

    @Schema(description = "店铺冻结资金")
    private BigDecimal frozenMoney;

    @Schema(description = "1，提现")
    private Integer type;

    @Schema(description = "改变原因")
    private String changeReasons;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "现店铺资金")
    private BigDecimal newShopMoney;

    @Schema(description = "现店铺冻结资金")
    private BigDecimal newFrozenMoney;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "店铺标题")
    private String shopTitle;

    @Schema(description = "订单已支付金额汇总")
    private BigDecimal unSettlementOrder;

    @Schema(description = "变更金额")
    private BigDecimal changeAmount;

    @Schema(description = "资金类型：1 线下，2 线上")
    private Integer balanceType;

    @Schema(description = "线上渠道：WECHAT/HUIFU/ALIPAY")
    private String payChannel;
}
