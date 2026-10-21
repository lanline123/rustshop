package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.YaBandPaySettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.YABANPAY_SECRET_KEY;

/**
 * 系统设置-YaBandPay支付设置新增参数
 *
 * @author kidd
 * @since 2025/6/7 11:30
 */
@Data
public class YaBandPaySettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_YABANPAY)
    @Schema(description = "是否启用YaBand支付；0-关闭，1-开启")
    private Integer useYabanpay;

    @ConfigItemField(USE_YABANPAY_WECHAT)
    @Schema(description = "是否启用YaBand微信支付；0-关闭，1-开启")
    private Integer useYabanpayWechat;

    @ConfigItemField(USE_YABANPAY_ALIPAY)
    @Schema(description = "是否启用YaBand支付宝支付；0-关闭，1-开启")
    private Integer useYabanpayAlipay;

    @ConfigItemField(YABANPAY_CURRENCY)
    @Schema(description = "YaBand支付货币类型")
    private String yabanpayCurrency;

    @ConfigItemField(YABANPAY_UID)
    @Schema(description = "YaBand支付UID")
    private String yabandpayUid;

    @ConfigItemField(YABANPAY_SECRET_KEY)
    @Schema(description = "YaBand支付密钥")
    private String yabandpaySecretKey;

    public void noUpdate(YaBandPaySettingsVO settings) {
        if (Objects.equals(this.yabandpayUid, settings.getYabandpayUid())) {
            this.yabandpayUid = null;
        }
        if (Objects.equals(this.yabandpaySecretKey, settings.getYabandpaySecretKey())) {
            this.yabandpaySecretKey = null;
        }
    }

}
