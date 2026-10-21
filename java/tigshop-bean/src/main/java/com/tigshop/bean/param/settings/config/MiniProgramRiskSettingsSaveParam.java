package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序风控配置保存参数（与直播能力解耦，始终可用）
 */
@Data
public class MiniProgramRiskSettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.MINI_IMG_SEC_CHECK)
    @Schema(description = "图片鉴黄鉴违；0-关闭，1-开启")
    private Integer miniImgSecCheck;
}
