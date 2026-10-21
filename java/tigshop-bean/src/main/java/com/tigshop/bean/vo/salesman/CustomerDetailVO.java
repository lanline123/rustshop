package com.tigshop.bean.vo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端分销客户详情。
 *
 * <p>不包含关系更新时间、保护期剩余、有效期剩余等未落库字段。</p>
 */
@Data
@Schema(description = "用户端分销客户详情")
public class CustomerDetailVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    @Sensitive(type = SensitiveType.PHONE)
    private String mobile;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "会员等级名称")
    private String rankName;

    @Schema(description = "客户分类：ordered已支付且无售后订单客户，salesman下级分销员，customer普通客户")
    private String customerType;

    @Schema(description = "绑定时间，Unix秒")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private Long bindTime;

    @Schema(description = "客户与分销员建立绑定关系时记录的渠道；无绑定渠道时为空字符串")
    private String channel;

    @Schema(description = "当前分销员对客户的备注；无备注时为空字符串")
    private String remark = "";

    @Schema(description = "已支付且无售后订单数量")
    private Long orderCount = 0L;

    @Schema(description = "已支付订单实际成交净额（扣除实际退款）")
    private BigDecimal orderAmount = BigDecimal.ZERO;

    @Schema(description = "客单价")
    private BigDecimal averageOrderAmount = BigDecimal.ZERO;

    @Schema(description = "最后成交时间，Unix秒")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private Long lastOrderTime;
}
