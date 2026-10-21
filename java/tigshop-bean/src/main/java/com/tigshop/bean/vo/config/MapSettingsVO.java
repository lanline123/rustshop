// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.config;


import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 地图服务商统一设置。
 * <p>
 * 既有 {@link AmapAllVO} 保持原样不动，本类是新增的聚合视图：服务商选择 + 高德 6 项 + 天地图 2 项。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "地图服务商设置")
public class MapSettingsVO {
    @ConfigItemField(SettingsEnum.MAP_PROVIDER)
    @Schema(description = "地图服务商：amap-高德地图 tianditu-天地图")
    private String mapProvider;

    @ConfigItemField(SettingsEnum.AMAP_WEB_KEY)
    @Schema(description = "高德地图Key")
    private String amapWebKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapWebSecret;
    @ConfigItemField(SettingsEnum.AMAP_WEB_JS_KEY)
    @Schema(description = "高德地图JSKey")
    private String amapJsKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_JS_SECRET)
    @Schema(description = "高德地图JSSECRET")
    private String amapJsSecret;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_KEY)
    @Schema(description = "高德地图小程序Key")
    private String amapMiniKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_SECRET)
    @Schema(description = "高德地图小程序SECRET")
    private String amapMiniSecret;

    @ConfigItemField(SettingsEnum.TIANDITU_WEB_TK)
    @Schema(description = "天地图服务器端tk")
    private String tiandituWebTk;
    @ConfigItemField(SettingsEnum.TIANDITU_JS_TK)
    @Schema(description = "天地图浏览器端tk（JS API）")
    private String tiandituJsTk;

    /**
     * 掩码敏感值后再返回给前端。{@code mapProvider} 本身不是凭据，不掩码。
     */
    public void encryptData() {
        this.amapWebKey = StringUtils.maskMiddleHalf(this.amapWebKey);
        this.amapWebSecret = StringUtils.maskMiddleHalf(this.amapWebSecret);
        this.amapJsKey = StringUtils.maskMiddleHalf(this.amapJsKey);
        this.amapJsSecret = StringUtils.maskMiddleHalf(this.amapJsSecret);
        this.amapMiniKey = StringUtils.maskMiddleHalf(this.amapMiniKey);
        this.amapMiniSecret = StringUtils.maskMiddleHalf(this.amapMiniSecret);
        this.tiandituWebTk = StringUtils.maskMiddleHalf(this.tiandituWebTk);
        this.tiandituJsTk = StringUtils.maskMiddleHalf(this.tiandituJsTk);
    }
}
