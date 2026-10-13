// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.o2o;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.INTRA_CITY_DELIVERY_ENABLED;
import static com.tigshop.bean.enums.setting.SettingsEnum.INTRA_CITY_SELF_DELIVERY_ENABLED;
import static com.tigshop.bean.enums.setting.SettingsEnum.INTRA_CITY_THIRD_PARTY_DELIVERY_ENABLED;

/**
 * 平台级同城配送总控配置保存参数。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "平台级同城配送总控配置保存参数")
public class IntraCityDeliverySettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(INTRA_CITY_DELIVERY_ENABLED)
    @Schema(description = "同城配送功能总开关；0-关闭 1-启用")
    private Integer intraCityDeliveryEnabled;

    @ConfigItemField(INTRA_CITY_SELF_DELIVERY_ENABLED)
    @Schema(description = "启用商家自配送（自建配送员运力）；0-关闭 1-启用")
    private Integer intraCitySelfDeliveryEnabled;

    @ConfigItemField(INTRA_CITY_THIRD_PARTY_DELIVERY_ENABLED)
    @Schema(description = "启用第三方配送（美团等第三方运力）；0-关闭 1-启用")
    private Integer intraCityThirdPartyDeliveryEnabled;
}
