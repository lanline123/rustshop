package com.tigshop.service.order;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 线上微信分账开启时，阻止未完成微信进件的店铺进入购买链路。 */
@Component
@RequiredArgsConstructor
public class OrderOnboardingGuard {
    private final ConfigService configService;
    private final SubMerchantService subMerchantService;

    public void assertShopCanPurchase(Integer shopId) {
        if (!isOnlineWechatProfitSharing() || shopId == null) return;
        SubMerchant merchant = subMerchantService.getActiveByShop(shopId.longValue(), "WECHAT");
        if (merchant == null || StrUtil.isBlank(merchant.getSubMchId())) {
            throw new GlobalException("该店铺尚未完成微信分账进件，暂不支持购买");
        }
    }

    private boolean isOnlineWechatProfitSharing() {
        if (!configService.isProfitSharingEnabled()) return false;
        String mchidType = configService.getConfigVal(SettingsEnum.WECHAT_MCHID_TYPE);
        String splitMethod = configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD);
        String channel = configService.getConfigVal(SettingsEnum.PROFIT_SHARING_ONLINE_CHANNEL);
        return "2".equals(StrUtil.trimToEmpty(mchidType))
                && "2".equals(StrUtil.trimToEmpty(splitMethod))
                && (StrUtil.isBlank(channel) || "wechat".equalsIgnoreCase(StrUtil.trim(channel)));
    }
}
