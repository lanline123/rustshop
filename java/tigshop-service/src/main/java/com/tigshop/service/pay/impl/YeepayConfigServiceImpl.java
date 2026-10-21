package com.tigshop.service.pay.impl;

import com.tigshop.bean.param.settings.config.YeepaySettingsSaveParam;
import com.tigshop.bean.vo.setting.config.YeepaySettingsVO;
import com.tigshop.service.pay.YeepayConfigService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 易宝支付（YOP）后台配置读写实现（VORT 门控 common.pay.yeepayPay 专属，整文件可删）。
 *
 * @author Tigshop团队
 * @since 2026/08/06
 */
@Service
@RequiredArgsConstructor
public class YeepayConfigServiceImpl implements YeepayConfigService {

    private final ConfigService configService;

    @Override
    public YeepaySettingsVO getYeepaySettings() {
        YeepaySettingsVO settings = configService.getSettings(YeepaySettingsVO.class);
        // 数据加密
        settings.encryptData();
        return settings;
    }

    @Transactional
    @Override
    public void saveYeepaySettings(YeepaySettingsSaveParam param) {
        YeepaySettingsVO settings = this.getYeepaySettings();
        param.noUpdate(settings);
        configService.saveConfigSettings(param);
    }
}
