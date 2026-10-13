package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序风控配置 VO（与直播能力解耦，始终可用）
 */
@Data
public class MiniProgramRiskSettingsVO {

    @ConfigItemField(SettingsEnum.MINI_IMG_SEC_CHECK)
    @Schema(description = "图片鉴黄鉴违；0-关闭，1-开启")
    private Integer miniImgSecCheck;
}
