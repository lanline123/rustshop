package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺端保证金总览VO。
 *
 * <p>金额三数与应缴/已缴均来自 {@code DepositAccountService} 实时口径，不在本模块落库。</p>
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺端保证金总览")
public class DepositShopOverviewVO {

    @Schema(description = "应缴保证金(computeRequired)")
    private BigDecimal required;

    @Schema(description = "已缴保证金(paid)")
    private BigDecimal paid;

    @Schema(description = "待补缴 = max(0, 应缴-已缴)")
    private BigDecimal pending;

    @Schema(description = "可提现 = max(0, 已缴-应缴)")
    private BigDecimal withdrawable;

    @Schema(description = "待审核提现金额 = 待审核退还/提现申请金额合计")
    private BigDecimal pendingWithdraw;

    @Schema(description = "冻结金额 = min(已缴, 应缴)")
    private BigDecimal frozen;

    @Schema(description = "账户状态(shop_deposit.status)")
    private Integer status;

    @Schema(description = "扣罚状态(shop_deposit.penalty_status,0正常)")
    private Integer penaltyStatus;

    @Schema(description = "派生状态文案：正常/待补缴/已处罚")
    private String statusText;

    @Schema(description = "补缴到期时间戳(秒,supplement_due_time,0未触发)")
    private Long supplementDueTime;

    @Schema(description = "补缴到期时间(格式化,空=未触发)")
    private String supplementDueTimeText;

    @Schema(description = "是否存在保证金协议(开关开启且内容非空)")
    private Boolean hasProtocol;
}
