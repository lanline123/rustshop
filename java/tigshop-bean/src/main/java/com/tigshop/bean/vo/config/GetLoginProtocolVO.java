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
public class GetLoginProtocolVO {

    @ConfigItemField(SettingsEnum.TERMS_OF_SERVICE_SHOW)
    @Schema(description = "服务协议展示")
    private Integer termsOfServiceShow;

    @ConfigItemField(SettingsEnum.PRIVACY_POLICY_SHOW)
    @Schema(description = "隐私政策展示")
    private Integer privacyPolicyShow;

    @ConfigItemField(SettingsEnum.AFTER_SALES_SERVICE_SHOW)
    @Schema(description = "售后服务展示")
    private Integer afterSalesServiceShow;
}
