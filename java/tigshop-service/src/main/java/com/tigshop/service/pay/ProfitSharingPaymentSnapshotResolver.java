package com.tigshop.service.pay;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.bo.pay.ProfitSharingPaymentContext;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 为普通订单的支付宝、汇付请求解析一次性分账渠道快照。 */
@Component
@RequiredArgsConstructor
public class ProfitSharingPaymentSnapshotResolver {

    private final ConfigService configService;

    public enum PaymentScene {
        ORDER,
        POS,
        RECHARGE,
        DEPOSIT
    }

    public ProfitSharingPaymentContext resolve(String payType, String clientType, PaymentScene scene) {
        if (scene != PaymentScene.ORDER) {
            return ProfitSharingPaymentContext.none();
        }
        boolean alipay = "alipay".equals(payType);
        boolean huifu = "huifuWechat".equals(payType) || "huifuAlipay".equals(payType);
        if (!alipay && !huifu) {
            return ProfitSharingPaymentContext.none();
        }
        if (!configService.isProfitSharingEnabled()
                || !"2".equals(configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD))) {
            return ProfitSharingPaymentContext.none();
        }

        String selectedChannel = configService.getConfigVal(SettingsEnum.PROFIT_SHARING_ONLINE_CHANNEL);
        if (alipay) {
            if (!"alipay".equals(selectedChannel)
                    || !"1".equals(configService.getConfigVal(SettingsEnum.ALIPAY_PROFIT_SHARING_ENABLED))
                    || !"1".equals(configService.getConfigVal(SettingsEnum.ALIPAY_DELAY_ACCT_ENABLED))) {
                return ProfitSharingPaymentContext.none();
            }
            return ProfitSharingPaymentContext.of("ALIPAY");
        }

        if (!"huifu".equals(selectedChannel)
                || !"1".equals(configService.getConfigVal(SettingsEnum.HUIFU_PROFIT_SHARING_ENABLED))
                || !"1".equals(configService.getConfigVal(SettingsEnum.HUIFU_DELAY_ACCT_ENABLED))) {
            return ProfitSharingPaymentContext.none();
        }
        if ("pc".equals(clientType)
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.HUIFU_HOSTING_PROJECT_ID))) {
            return ProfitSharingPaymentContext.none();
        }
        return ProfitSharingPaymentContext.of("HUIFU");
    }
}
