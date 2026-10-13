package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺端保证金资金明细VO（shop_deposit_log 全类型流水视图）。
 *
 * <p>按 CR-20260616-135500：原"冻结明细=平台扣款记录(type=4)"扩展为保证金资金明细，
 * 展示全部资金流水（充值/提现/退还/扣除/调整），用 type+typeText 区分资金类型；
 * 冻结金额(数字)仍由 {@code DepositAccountService.frozen} 实时算。</p>
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺端保证金资金明细")
public class DepositShopFrozenDetailVO {

    @Schema(description = "流水ID")
    private Integer shopDepositLogId;

    @Schema(description = "资金类型 DepositLogType：1充值 2提现 3退还 4平台扣除 5平台调整")
    private Integer type;

    @Schema(description = "资金类型文案")
    private String typeText;

    @Schema(description = "扣款金额(正数,展示用;非扣款类为变动金额绝对值)")
    private BigDecimal deductAmount;

    @Schema(description = "原始变动金额(带符号,通常为负)")
    private BigDecimal changeAmount;

    @Schema(description = "原因/备注(流水备注)")
    private String reason;

    @Schema(description = "关联订单ID")
    private Integer orderId;

    @Schema(description = "关联单ID(充值/退还单等,业务编号展示用)")
    private Integer relatedId;

    @Schema(description = "扣款时间戳(秒)")
    private Long addTime;

    @Schema(description = "扣款时间(格式化)")
    private String addTimeText;
}
