package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺端保证金交易流水VO（来源 shop_deposit_log，仅当前店铺）。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺端保证金交易流水")
public class DepositShopLogVO {

    @Schema(description = "流水ID")
    private Integer shopDepositLogId;

    @Schema(description = "交易类型(DepositLogType)")
    private Integer type;

    @Schema(description = "交易类型文案")
    private String typeText;

    @Schema(description = "变动金额(带符号)")
    private BigDecimal changeAmount;

    @Schema(description = "变动前实缴")
    private BigDecimal beforePaid;

    @Schema(description = "变动后实缴")
    private BigDecimal afterPaid;

    @Schema(description = "关联订单ID(平台扣除/赔付时)")
    private Integer orderId;

    @Schema(description = "关联单ID(充值/退还单等)")
    private Integer relatedId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发生时间戳(秒)")
    private Long addTime;

    @Schema(description = "发生时间(格式化)")
    private String addTimeText;

    @Schema(description = "来源账户（如充值打款账户、扣款时的保证金余额）")
    private String sourceAccount;

    @Schema(description = "去向账户（如充值入账的保证金余额、扣款至平台账户）")
    private String destAccount;
}
