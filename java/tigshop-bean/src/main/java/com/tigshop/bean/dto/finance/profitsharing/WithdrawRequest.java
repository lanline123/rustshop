package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 提现请求
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class WithdrawRequest {

    /** 子商户号 */
    private String subMchId;

    /** 提现金额-元 */
    private BigDecimal amount;

    /** 商户请求号 */
    private String outRequestNo;

    /** 备注 */
    private String remark;

    /** 收款方账号（支付宝转账出金用：真实支付宝登录号/UID，取自提现账户数据，非 smid） */
    private String payeeAccount;

    /** 收款方户名（转账校验用，可选） */
    private String payeeName;

    /** 收款方身份类型（如 ALIPAY_LOGON_ID / ALIPAY_USER_ID；空则由渠道默认） */
    private String payeeType;
}
