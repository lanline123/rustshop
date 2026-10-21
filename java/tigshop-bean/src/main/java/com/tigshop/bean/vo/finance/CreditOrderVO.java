package com.tigshop.bean.vo.finance;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账期订单VO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "账期订单")
public class CreditOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "账期订单ID")
    private Integer creditOrderId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员用户名")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "账期支付金额")
    private BigDecimal creditAmount;

    @Schema(description = "已退款金额（账期支付退款累计）")
    private BigDecimal refundedAmount;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "订单状态文本")
    private String orderStatusText;

    @Schema(description = "结算状态：0-待结算，1-已结算")
    private Integer settlementStatus;

    @Schema(description = "结算状态文本")
    private String settlementStatusText;

    @Schema(description = "回款流水单号")
    private String settlementSn;

    @Schema(description = "下单时间")
    private String orderTime;

    @Schema(description = "结算时间")
    private String settlementTime;
}
