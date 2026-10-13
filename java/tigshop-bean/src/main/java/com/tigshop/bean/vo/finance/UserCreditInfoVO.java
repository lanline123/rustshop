package com.tigshop.bean.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户账期支付信息VO（用于订单确认页）
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "用户账期支付信息")
public class UserCreditInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否可使用账期支付")
    private Boolean canUseCredit;

    @Schema(description = "不可使用原因（canUseCredit=false时返回）")
    private String disableReason;

    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    @Schema(description = "已使用额度")
    private BigDecimal usedAmount;

    @Schema(description = "可用额度")
    private BigDecimal availableAmount;

    @Schema(description = "结算账期类型：1-月结，2-季结")
    private Integer settlementType;

    @Schema(description = "结算账期类型文本")
    private String settlementTypeText;

    @Schema(description = "账期天数：30或90")
    private Integer settlementDays;

    @Schema(description = "剩余账期天数")
    private Integer remainingDays;

    @Schema(description = "账期支付状态：1-正常，2-逾期，3-冻结")
    private Integer creditPaymentStatus;

    @Schema(description = "账期支付状态文本")
    private String creditPaymentStatusText;
}
