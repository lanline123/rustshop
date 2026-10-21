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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/10 16:21
 */
@Data
@Schema(description = "地图设置参数")
public class GetAmapMiniVO {
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_KEY)
    @Schema(description = "高德地图Key")
    private String amapKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapSecret;
}
