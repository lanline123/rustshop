package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 会员余额提现配置参数（与分账/佣金提现独立，D1107）
 *
 * @author Tigshop团队
 */
@Schema(description = "会员余额提现配置参数")
@Data
public class MemberWithdrawalSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.MEMBER_WITHDRAWAL_ENABLED)
    @Schema(description = "会员余额提现开关，0-关闭，1-开通")
    private Integer memberWithdrawalEnabled;

    @ConfigItemField(SettingsEnum.MEMBER_WITHDRAWAL_RECEIPT_METHOD)
    @Schema(description = "会员余额提现到账方式，数组：1-银行卡，2-支付宝，3-微信")
    private List<Integer> memberWithdrawalReceiptMethod;

    @ConfigItemField(SettingsEnum.MEMBER_WITHDRAWAL_DESCRIPTION)
    @Schema(description = "会员余额提现说明")
    private String memberWithdrawalDescription;
}
