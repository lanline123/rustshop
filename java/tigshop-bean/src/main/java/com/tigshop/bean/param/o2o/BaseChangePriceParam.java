package com.tigshop.bean.param.o2o;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tigshop.bean.param.order.OrderCheckParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "基础改价")
public class BaseChangePriceParam {

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Schema(description = "服务端 POS 业务场景标识", hidden = true)
    private boolean posCheckout;

    @Schema(description = "是否是从购物车列表的数据进行改价(DB中的数据)",hidden = true)
    private Boolean isCartList;

    @Schema(description = "只是改价(不进行存储)",hidden = true)
    private Boolean onlyShow;

    @Schema(description = "是否改价(购物车商品对应改价,总体改价(总体价格))")
    private Boolean useChangePrice;

    @Schema(description = "购物车改价列表(要和购物车进行对比)")
    private List<OrderCheckParam.CartChangePrice> cartChangePrices;

    @Schema(description = "整体改价之前的 原价")
    private BigDecimal originalPrice;

    @Schema(description = "改价的值(一口价就是价格,折扣就是折扣值(0-100),减少的值)")
    private BigDecimal changePriceValue;

    @Schema(description = "总体改价类型(1 一口价 2 折扣 3 减少)")
    private Integer totalChangePriceType;

    @Schema(description = "抹分金额")
    private Boolean roundOffCents;

    @Schema(description = "抹角金额")
    private Boolean roundOffDime;

    @Schema(description = "现金金额")
    private BigDecimal cashAmount;

    @Schema(description = "现金找零金额(现金金额(cashAmount)-支付的金额(amount))")
    private BigDecimal cashChangeAmount;

    /**
     * 由 POS 专属控制器标记业务场景，避免把登录身份与收银业务入口耦合。
     */
    public void markPosCheckout() {
        this.posCheckout = true;
    }

    @JsonIgnore
    public boolean isPosCheckout() {
        return posCheckout;
    }

}
