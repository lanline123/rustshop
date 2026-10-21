package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分销佣金提现配置（独立于会员余额提现，D1107）
 *
 * @author Tigshop团队
 */
@Schema(description = "分销佣金提现配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionWithdrawalSettingsVO {

    @ConfigItemField(SettingsEnum.COMMISSION_WITHDRAWAL_ENABLED)
    @Schema(description = "分销佣金提现开关，0-关闭，1-开通")
    private Integer commissionWithdrawalEnabled;

    @ConfigItemField(SettingsEnum.COMMISSION_WITHDRAWAL_RECEIPT_METHOD)
    @Schema(description = "佣金提现到账方式，数组：1-银行卡，2-支付宝，3-微信")
    private List<Integer> commissionWithdrawalReceiptMethod;

    @ConfigItemField(SettingsEnum.COMMISSION_WITHDRAWAL_DESCRIPTION)
    @Schema(description = "分销佣金提现说明")
    private String commissionWithdrawalDescription;
}
