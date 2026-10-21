package com.tigshop.service.pay;

import com.tigshop.bean.param.settings.config.ApplePaySettingsSaveParam;
import com.tigshop.bean.vo.setting.config.ApplePaySettingsVO;

/**
 * Apple Pay 后台配置读写服务（VORT 门控 common.pay.applePay 专属，整文件可删）。
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
public interface ApplePayConfigService {

    /**
     * 获取 Apple Pay 配置（含敏感字段掩码 + 国家/货币下拉列表解析）。
     */
    ApplePaySettingsVO getApplePaySettings();

    /**
     * 保存 Apple Pay 配置（开启时强校验必填项 BR-08）。
     */
    void saveApplePaySettings(ApplePaySettingsSaveParam param);
}
