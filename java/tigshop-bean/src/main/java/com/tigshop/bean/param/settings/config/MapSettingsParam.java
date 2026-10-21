// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.settings.config;


import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.config.MapSettingsVO;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 地图服务商统一设置保存参数。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "地图服务商设置参数")
public class MapSettingsParam implements ConfigSaveParam {
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
     * 提交值与库中真值的掩码串一致时，说明前端并未改动该项，置 null 使其不参与写库。
     * <p>
     * 缺失这一步会把 {@code ab****cd} 这样的掩码串当成真实凭据写库，属不可逆资损。
     *
     * @param settings 库中的真值（未掩码）
     */
    public void noUpdate(MapSettingsVO settings) {
        if (isUnchanged(this.amapWebKey, settings.getAmapWebKey())) {
            this.amapWebKey = null;
        }
        if (isUnchanged(this.amapWebSecret, settings.getAmapWebSecret())) {
            this.amapWebSecret = null;
        }
        if (isUnchanged(this.amapJsKey, settings.getAmapJsKey())) {
            this.amapJsKey = null;
        }
        if (isUnchanged(this.amapJsSecret, settings.getAmapJsSecret())) {
            this.amapJsSecret = null;
        }
        if (isUnchanged(this.amapMiniKey, settings.getAmapMiniKey())) {
            this.amapMiniKey = null;
        }
        if (isUnchanged(this.amapMiniSecret, settings.getAmapMiniSecret())) {
            this.amapMiniSecret = null;
        }
        if (isUnchanged(this.tiandituWebTk, settings.getTiandituWebTk())) {
            this.tiandituWebTk = null;
        }
        if (isUnchanged(this.tiandituJsTk, settings.getTiandituJsTk())) {
            this.tiandituJsTk = null;
        }
    }

    private static boolean isUnchanged(String submitted, String stored) {
        return Objects.equals(submitted, StringUtils.maskMiddleHalf(stored));
    }
}
