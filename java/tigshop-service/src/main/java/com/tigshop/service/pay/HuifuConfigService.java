package com.tigshop.service.pay;

import com.tigshop.bean.param.settings.config.HuifuSettingsSaveParam;
import com.tigshop.bean.vo.setting.config.HuifuSettingsVO;

/**
 * 汇付斗拱支付后台配置读写服务（VORT 门控 common.pay.huifuPay 专属，整文件可删）。
 *
 * <p>从共享 {@code ConfigService} 拆出，使 ConfigService/ConfigServiceImpl/ConfigController
 * 不再引用 {@link HuifuSettingsVO}/{@link HuifuSettingsSaveParam}，关闭汇付时整文件删除，
 * 无需在共享文件内联 {@code @vort-if}。</p>
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
public interface HuifuConfigService {

    /**
     * 获取汇付斗拱支付配置（含敏感字段掩码）。
     */
    HuifuSettingsVO getHuifuSettings();

    /**
     * 保存汇付斗拱支付配置。
     */
    void saveHuifuSettings(HuifuSettingsSaveParam param);
}
