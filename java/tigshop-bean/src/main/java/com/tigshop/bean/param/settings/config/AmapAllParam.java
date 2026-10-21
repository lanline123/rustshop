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
import com.tigshop.bean.vo.config.AmapAllVO;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * @author Tigshop团队
 * @create 2025/8/12 11:37
 */
@Data
@Schema(description = "门店设置参数")
public class AmapAllParam implements ConfigSaveParam {
    @ConfigItemField(SettingsEnum.AMAP_WEB_KEY)
    @Schema(description = "高德地图Key")
    private String amapWebKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapWebSecret;
    @ConfigItemField(SettingsEnum.AMAP_WEB_JS_KEY)
    @Schema(description = "高德地图Key")
    private String amapJsKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_JS_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapJsSecret;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_KEY)
    @Schema(description = "高德地图Key")
    private String amapMiniKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapMiniSecret;

    public void noUpdate(AmapAllVO amapAllVO) {
        if (Objects.equals(this.amapWebKey, StringUtils.maskMiddleHalf(amapAllVO.getAmapWebKey()))) {
            this.amapWebKey = null;
        }
        if (Objects.equals(this.amapWebSecret, StringUtils.maskMiddleHalf(amapAllVO.getAmapWebSecret()))) {
            this.amapWebSecret = null;
        }
        if (Objects.equals(this.amapJsKey, StringUtils.maskMiddleHalf(amapAllVO.getAmapJsKey()))) {
            this.amapJsKey = null;
        }
        if (Objects.equals(this.amapJsSecret, StringUtils.maskMiddleHalf(amapAllVO.getAmapJsSecret()))) {
            this.amapJsSecret = null;
        }
        if (Objects.equals(this.amapMiniKey, StringUtils.maskMiddleHalf(amapAllVO.getAmapMiniKey()))) {
            this.amapMiniKey = null;
        }
        if (Objects.equals(this.amapMiniSecret, StringUtils.maskMiddleHalf(amapAllVO.getAmapMiniSecret()))) {
            this.amapMiniSecret = null;
        }
    }
}
