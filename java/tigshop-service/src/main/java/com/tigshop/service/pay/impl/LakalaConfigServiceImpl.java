package com.tigshop.service.pay.impl;

import com.tigshop.bean.param.settings.config.LakalaSettingsSaveParam;
import com.tigshop.bean.vo.setting.config.LakalaSettingsVO;
import com.tigshop.service.pay.LakalaConfigService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 拉卡拉支付后台配置读写实现（VORT 门控 common.pay.lakalaPay 专属，整文件可删）。
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@Service
@RequiredArgsConstructor
public class LakalaConfigServiceImpl implements LakalaConfigService {

    private final ConfigService configService;

    @Override
    public LakalaSettingsVO getLakalaSettings() {
        LakalaSettingsVO settings = configService.getSettings(LakalaSettingsVO.class);
        // 数据加密（私钥 / 证书 / SM4 密钥掩码）
        settings.encryptData();
        return settings;
    }

    @Transactional
    @Override
    public void saveLakalaSettings(LakalaSettingsSaveParam param) {
        LakalaSettingsVO settings = this.getLakalaSettings();
        param.noUpdate(settings);
        configService.saveConfigSettings(param);
    }
}
