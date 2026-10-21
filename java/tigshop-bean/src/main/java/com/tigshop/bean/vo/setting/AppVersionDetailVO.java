package com.tigshop.bean.vo.setting;

import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * app 版本详情
 *
 * @author kidd
 * @since 2025/4/10 17:43
 */
@Data
public class AppVersionDetailVO {

    @Schema(description = "Android版本号")
    private String androidVersion;

    @Schema(description = "iOS版本号")
    private String iosVersion;

    @Schema(description = "iOS下载链接")
    private String iosLink;

    @Schema(description = "Android下载链接")
    private String androidLink;

    public AppVersionDetailVO(Map<String, String> settingsMap) {
        this.androidVersion = settingsMap.get(SettingsEnum.ANDROID_VERSION.getBizCode());
        this.iosVersion = settingsMap.get(SettingsEnum.IOS_VERSION.getBizCode());
        this.iosLink = settingsMap.get(SettingsEnum.IOS_LINK.getBizCode());
        this.androidLink = settingsMap.get(SettingsEnum.ANDROID_LINK.getBizCode());
    }

}
