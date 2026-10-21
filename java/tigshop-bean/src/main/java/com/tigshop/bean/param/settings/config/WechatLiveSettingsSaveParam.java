package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.WechatLiveSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 小程序直播/视频号 独立小程序配置保存参数
 */
@Data
public class WechatLiveSettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.WECHAT_LIVE_MINI_APP_ID)
    @Schema(description = "小程序直播AppID")
    private String wechatLiveMiniAppId;

    @ConfigItemField(SettingsEnum.WECHAT_LIVE_MINI_APP_SECRET)
    @Schema(description = "小程序直播AppSecret")
    private String wechatLiveMiniAppSecret;

    public void noUpdate(WechatLiveSettingsVO settings) {
        if (Objects.equals(this.wechatLiveMiniAppId, settings.getWechatLiveMiniAppId())) {
            this.wechatLiveMiniAppId = null;
        }
        if (Objects.equals(this.wechatLiveMiniAppSecret, settings.getWechatLiveMiniAppSecret())) {
            this.wechatLiveMiniAppSecret = null;
        }
    }
}
