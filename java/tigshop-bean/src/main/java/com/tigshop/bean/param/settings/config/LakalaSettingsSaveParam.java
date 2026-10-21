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
import com.tigshop.bean.vo.setting.config.LakalaSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-拉卡拉支付设置保存参数
 *
 * @author Tigshop团队
 * @since 2026/07
 */
@Data
public class LakalaSettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_LAKALA)
    @Schema(description = "是否启用拉卡拉支付；0-关闭，1-开启")
    private Integer useLakala;

    @ConfigItemField(LAKALA_APP_ID)
    @Schema(description = "拉卡拉开放平台 AppId")
    private String lakalaAppId;

    @ConfigItemField(LAKALA_MERCHANT_NO)
    @Schema(description = "拉卡拉商户号（merchantNo）")
    private String lakalaMerchantNo;

    @ConfigItemField(LAKALA_TERM_NO)
    @Schema(description = "拉卡拉终端号（termNo）")
    private String lakalaTermNo;

    @ConfigItemField(LAKALA_SERIAL_NO)
    @Schema(description = "拉卡拉商户证书序列号（serialNo）")
    private String lakalaSerialNo;

    @ConfigItemField(LAKALA_MERCHANT_PRIVATE_KEY)
    @Schema(description = "拉卡拉商户私钥（字符串，PKCS8）")
    private String lakalaMerchantPrivateKey;

    @ConfigItemField(LAKALA_PLATFORM_CERT)
    @Schema(description = "拉卡拉开放平台证书（字符串，.cer 内容）")
    private String lakalaPlatformCert;

    @ConfigItemField(LAKALA_SM4_KEY)
    @Schema(description = "拉卡拉报文对称加密 SM4 密钥（可选）")
    private String lakalaSm4Key;

    @ConfigItemField(LAKALA_GATEWAY_URL)
    @Schema(description = "拉卡拉网关地址")
    private String lakalaGatewayUrl;

    @ConfigItemField(LAKALA_SANDBOX)
    @Schema(description = "拉卡拉是否启用沙箱/测试环境；0-生产，1-沙箱")
    private Integer lakalaSandbox;

    @ConfigItemField(LAKALA_WX_SUB_APPID)
    @Schema(description = "拉卡拉微信子 AppId（公众号 JSAPI）")
    private String lakalaWxSubAppid;

    @ConfigItemField(LAKALA_WX_MINI_APPID)
    @Schema(description = "拉卡拉微信小程序 AppId")
    private String lakalaWxMiniAppid;

    /**
     * 提交时若敏感字段值与库内掩码后的值一致，认为未做修改，置 null 跳过更新，
     * 避免把"星号占位串"覆盖回真实私钥 / 证书 / 密钥。
     */
    public void noUpdate(LakalaSettingsVO settings) {
        if (Objects.equals(this.lakalaMerchantPrivateKey, settings.getLakalaMerchantPrivateKey())) {
            this.lakalaMerchantPrivateKey = null;
        }
        if (Objects.equals(this.lakalaPlatformCert, settings.getLakalaPlatformCert())) {
            this.lakalaPlatformCert = null;
        }
        if (Objects.equals(this.lakalaSm4Key, settings.getLakalaSm4Key())) {
            this.lakalaSm4Key = null;
        }
    }
}
