package com.tigshop.service.pay;

import com.tigshop.bean.param.settings.config.LakalaSettingsSaveParam;
import com.tigshop.bean.vo.setting.config.LakalaSettingsVO;

/**
 * 拉卡拉支付后台配置读写服务（VORT 门控 common.pay.lakalaPay 专属，整文件可删）。
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
public interface LakalaConfigService {

    /**
     * 获取拉卡拉支付配置（含敏感字段掩码）。
     */
    LakalaSettingsVO getLakalaSettings();

    /**
     * 保存拉卡拉支付配置。
     */
    void saveLakalaSettings(LakalaSettingsSaveParam param);
}
