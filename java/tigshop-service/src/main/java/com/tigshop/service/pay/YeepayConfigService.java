package com.tigshop.service.pay;

import com.tigshop.bean.param.settings.config.YeepaySettingsSaveParam;
import com.tigshop.bean.vo.setting.config.YeepaySettingsVO;

/**
 * 易宝支付（YOP）后台配置读写服务（VORT 门控 common.pay.yeepayPay 专属，整文件可删）。
 *
 * <p>从共享 {@code ConfigService} 拆出，使 ConfigService/ConfigServiceImpl/ConfigController
 * 不再引用 {@link YeepaySettingsVO}/{@link YeepaySettingsSaveParam}，关闭易宝时整文件删除，
 * 无需在共享文件内联 {@code @vort-if}。</p>
 *
 * @author Tigshop团队
 * @since 2026/08/06
 */
public interface YeepayConfigService {

    /**
     * 获取易宝支付配置（含敏感字段掩码）。
     */
    YeepaySettingsVO getYeepaySettings();

    /**
     * 保存易宝支付配置。
     */
    void saveYeepaySettings(YeepaySettingsSaveParam param);
}
