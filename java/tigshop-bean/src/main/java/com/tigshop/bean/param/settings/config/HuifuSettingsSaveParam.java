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
import com.tigshop.bean.vo.setting.config.HuifuSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-汇付斗拱支付设置保存参数
 *
 * @author Tigshop团队
 * @since 2026/07/15
 */
@Data
public class HuifuSettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_HUIFU)
    @Schema(description = "是否启用汇付支付；0-关闭，1-开启（开启后微信/支付宝均可用）")
    private Integer useHuifu;

    @ConfigItemField(HUIFU_SYS_ID)
    @Schema(description = "汇付系统号（服务商sys_id）")
    private String huifuSysId;

    @ConfigItemField(HUIFU_PRODUCT_ID)
    @Schema(description = "汇付产品号（product_id）")
    private String huifuProductId;

    @ConfigItemField(HUIFU_MERCHANT_ID)
    @Schema(description = "汇付商户号（平台服务商huifu_id）")
    private String huifuMerchantId;

    @ConfigItemField(HUIFU_RSA_PRIVATE_KEY)
    @Schema(description = "汇付商户RSA私钥（加签用）")
    private String huifuRsaPrivateKey;

    @ConfigItemField(HUIFU_PUBLIC_KEY)
    @Schema(description = "汇付公钥（验签用）")
    private String huifuPublicKey;

    @ConfigItemField(HUIFU_GATEWAY)
    @Schema(description = "汇付API网关地址")
    private String huifuGateway;

    @ConfigItemField(HUIFU_PROFIT_SHARING_ENABLED)
    @Schema(description = "是否启用汇付分账；0-关闭，1-开启")
    private Integer huifuProfitSharingEnabled;

    @ConfigItemField(HUIFU_DELAY_ACCT_ENABLED)
    @Schema(description = "汇付是否启用延时冻结；0-否，1-是")
    private Integer huifuDelayAcctEnabled;

    @ConfigItemField(HUIFU_PROFIT_SHARING_NOTIFY_URL)
    @Schema(description = "汇付分账回调地址")
    private String huifuProfitSharingNotifyUrl;

    @ConfigItemField(HUIFU_PROFIT_SHARING_MAX_RATIO)
    @Schema(description = "汇付默认最大分账比例(%)")
    private String huifuProfitSharingMaxRatio;

    @ConfigItemField(HUIFU_PROFIT_SHARING_RULE_ORIGIN)
    @Schema(description = "汇付分账规则来源；01-接口发起，02-控台配置")
    private String huifuProfitSharingRuleOrigin;

    @ConfigItemField(HUIFU_HOSTING_PROJECT_ID)
    @Schema(description = "汇付统一收银台托管项目号（project_id，PC 收银台用；留空则不下发）")
    private String huifuHostingProjectId;

    /**
     * 提交时若敏感字段值与库内掩码后的值一致，认为未做修改，置 null 跳过更新，
     * 避免把"星号占位串"覆盖回真实密钥。
     */
    public void noUpdate(HuifuSettingsVO settings) {
        if (Objects.equals(this.huifuRsaPrivateKey, settings.getHuifuRsaPrivateKey())) {
            this.huifuRsaPrivateKey = null;
        }
        if (Objects.equals(this.huifuPublicKey, settings.getHuifuPublicKey())) {
            this.huifuPublicKey = null;
        }
    }
}
