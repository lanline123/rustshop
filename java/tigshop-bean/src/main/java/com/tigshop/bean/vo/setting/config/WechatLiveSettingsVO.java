package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序直播/视频号 独立小程序配置 VO
 */
@Data
public class WechatLiveSettingsVO {

    @ConfigItemField(SettingsEnum.WECHAT_LIVE_MINI_APP_ID)
    @Schema(description = "小程序直播AppID")
    private String wechatLiveMiniAppId;

    @ConfigItemField(SettingsEnum.WECHAT_LIVE_MINI_APP_SECRET)
    @Schema(description = "小程序直播AppSecret")
    private String wechatLiveMiniAppSecret;

    public void encryptData() {
        this.wechatLiveMiniAppId = StringUtils.maskMiddleHalf(this.wechatLiveMiniAppId);
        this.wechatLiveMiniAppSecret = StringUtils.maskMiddleHalf(this.wechatLiveMiniAppSecret);
    }
}
