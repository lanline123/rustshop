package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值支付数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "充值支付参数")
public class UserRechargeOrderPayDTO {
    @Schema(description = "订单ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "支付类型(cash,wechat,alipay)")
    private String type;

    @Schema(description = "现金金额(cash的时候才有效)")
    private BigDecimal cashAmount;

    @Schema(description = "现金找零金额((cash的时候才有效))(现金金额(cashAmount)-充值的金额(amount))")
    private BigDecimal cashChangeAmount;

    @Schema(description = "code")
    private String code;

    @Schema(description = "付款码(在线支付模式付款码支付不能为空)")
    private String authCode;
}
