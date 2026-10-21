package com.tigshop.service.pay.impl;

import com.tigshop.bean.param.settings.config.HuifuSettingsSaveParam;
import com.tigshop.bean.vo.setting.config.HuifuSettingsVO;
import com.tigshop.service.pay.HuifuConfigService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 汇付斗拱支付后台配置读写实现（VORT 门控 common.pay.huifuPay 专属，整文件可删）。
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@Service
@RequiredArgsConstructor
public class HuifuConfigServiceImpl implements HuifuConfigService {

    private final ConfigService configService;

    @Override
    public HuifuSettingsVO getHuifuSettings() {
        HuifuSettingsVO settings = configService.getSettings(HuifuSettingsVO.class);
        // 数据加密
        settings.encryptData();
        return settings;
    }

    @Transactional
    @Override
    public void saveHuifuSettings(HuifuSettingsSaveParam param) {
        HuifuSettingsVO settings = this.getHuifuSettings();
        param.noUpdate(settings);
        configService.saveConfigSettings(param);
    }
}
