package com.tigshop.service.product;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.finance.MerchantApplicationStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.MerchantApplication;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.finance.MerchantApplicationMapper;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductOnboardingGuard {
    private final MerchantApplicationMapper merchantApplicationMapper;
    private final ConfigService configService;

    public void assertShopFinished(Integer shopId) {
        if (!configService.isProfitSharingEnabled()
                || !"2".equals(configService.getConfigVal(SettingsEnum.WECHAT_MCHID_TYPE))
                || !"2".equals(configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD))) {
            return;
        }
        MerchantApplication latest = merchantApplicationMapper.selectOne(Wrappers.<MerchantApplication>lambdaQuery()
                .select(MerchantApplication::getStatus)
                .eq(MerchantApplication::getApplicantType, "SHOP")
                .eq(MerchantApplication::getApplicantId, shopId == null ? -1L : shopId.longValue())
                .orderByDesc(MerchantApplication::getId).last("limit 1"));
        if (latest == null || !MerchantApplicationStatus.FINISH.getCode().equals(latest.getStatus())) {
            throw new GlobalException("店铺尚未完成微信进件，无法上架商品");
        }
    }
}
