package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 退款时的分账回退基准（按支付商在各自 payRefund 内联回退平台服务费用）。
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class RefundSplitReversal {

    /** 是否需要回退（存在成功分账且比例服务费 > 0） */
    private boolean needsReversal;

    /** 需从平台回退的服务费（按退款额占比计算，元） */
    private BigDecimal platformServiceFee;

    /** 店铺子商户号（回退资金流入方） */
    private String subMchId;
}
