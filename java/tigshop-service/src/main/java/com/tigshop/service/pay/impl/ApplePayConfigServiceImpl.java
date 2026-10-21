package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.param.settings.config.ApplePaySettingsSaveParam;
import com.tigshop.bean.vo.setting.config.ApplePaySettingsVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.pay.ApplePayConfigService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Apple Pay 后台配置读写实现（VORT 门控 common.pay.applePay 专属，整文件可删）。
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@Service
@RequiredArgsConstructor
public class ApplePayConfigServiceImpl implements ApplePayConfigService {

    private final ConfigService configService;

    private final MessageHelper messageHelper;

    @Override
    public ApplePaySettingsVO getApplePaySettings() {
        ApplePaySettingsVO settings = configService.getSettings(ApplePaySettingsVO.class);

        // 数据加密（收单商户号/密钥中段掩码）
        settings.encryptData();

        settings.setApplePayCountryList(JSON.parseArray(settings.getApplePayCountryListStr()));
        settings.setApplePayCurrencyList(JSON.parseArray(settings.getApplePayCurrencyListStr()));

        return settings;
    }

    @Transactional
    @Override
    public void saveApplePaySettings(ApplePaySettingsSaveParam param) {
        ApplePaySettingsVO settings = this.getApplePaySettings();

        // 开启时强校验必填项（BR-08）：Merchant ID、展示名、商户身份证书、收单参数完整才允许开启。
        // 处理证书私钥（type6）仅 generic（自解密）必填；adyen/ums 由对方解密，无需该证书。
        if (Integer.valueOf(1).equals(param.getUseApplePay())) {
            // 有效收单机构：本次提交优先，其次沿用旧值，兜底 generic
            String provider = StrUtil.blankToDefault(param.getApplePayAcquirerProvider(),
                    StrUtil.blankToDefault(settings.getApplePayAcquirerProvider(), "generic"));
            boolean needProcessingKey = "generic".equalsIgnoreCase(provider);

            boolean complete = StrUtil.isNotBlank(param.getApplePayMerchantId())
                    && StrUtil.isNotBlank(param.getApplePayDisplayName())
                    && Integer.valueOf(1).equals(param.getApplePayMerchantIdentityCert())
                    && Integer.valueOf(1).equals(param.getApplePayMerchantIdentityKey())
                    && (!needProcessingKey
                            || Integer.valueOf(1).equals(param.getApplePayProcessingPrivateKey()))
                    && StrUtil.isNotBlank(param.getApplePayAcquirerGateway())
                    // 收单商户号/密钥：本次提交为掩码未改（null）时视为已存在旧值
                    && (StrUtil.isNotBlank(param.getApplePayAcquirerMerchantNo())
                            || StrUtil.isNotBlank(settings.getApplePayAcquirerMerchantNo()))
                    && (StrUtil.isNotBlank(param.getApplePayAcquirerKey())
                            || StrUtil.isNotBlank(settings.getApplePayAcquirerKey()));
            if (!complete) {
                throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
            }
        }

        param.noUpdate(settings);

        configService.saveConfigSettings(param);
    }
}
