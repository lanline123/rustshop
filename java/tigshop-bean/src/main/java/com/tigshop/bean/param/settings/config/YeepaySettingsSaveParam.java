// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.YeepaySettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-易宝支付（YOP）设置保存参数
 *
 * @author Tigshop团队
 * @since 2026/08/06
 */
@Data
public class YeepaySettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_YEEPAY)
    @Schema(description = "是否启用易宝支付；0-关闭，1-开启（开启后微信/支付宝均可用）")
    private Integer useYeepay;

    @ConfigItemField(YEEPAY_APP_KEY)
    @Schema(description = "易宝应用 AppKey（app_xxx）")
    private String yeepayAppKey;

    @ConfigItemField(YEEPAY_PARENT_MERCHANT_NO)
    @Schema(description = "易宝发起方商户编号（平台/服务商 parentMerchantNo）")
    private String yeepayParentMerchantNo;

    @ConfigItemField(YEEPAY_MERCHANT_NO)
    @Schema(description = "易宝收款商户编号（merchantNo）")
    private String yeepayMerchantNo;

    @ConfigItemField(YEEPAY_ISV_PRIVATE_KEY)
    @Schema(description = "易宝商户 RSA 私钥（加签/信封解密用，PKCS#8 Base64）")
    private String yeepayIsvPrivateKey;

    @ConfigItemField(YEEPAY_SERVER_ROOT)
    @Schema(description = "易宝网关地址（留空用 SDK 默认生产地址；沙箱联调时填沙箱地址）")
    private String yeepayServerRoot;

    /**
     * 提交时若敏感字段值与库内掩码后的值一致，认为未做修改，置 null 跳过更新，
     * 避免把"星号占位串"覆盖回真实密钥。
     */
    public void noUpdate(YeepaySettingsVO settings) {
        if (Objects.equals(this.yeepayIsvPrivateKey, settings.getYeepayIsvPrivateKey())) {
            this.yeepayIsvPrivateKey = null;
        }
    }
}
