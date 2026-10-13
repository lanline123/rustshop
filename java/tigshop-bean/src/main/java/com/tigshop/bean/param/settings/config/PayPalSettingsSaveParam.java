package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.PayPalSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.PAYPAL_SECRET;

/**
 * 系统设置-PayPal支付设置新增参数
 *
 * @author kidd
 * @since 2025/6/7 11:53
 */
@Data
public class PayPalSettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_PAYPAL)
    @Schema(description = "是否启用PayPal支付；0-关闭，1-开启")
    private Integer usePaypal;

    @ConfigItemField(PAYPAL_CURRENCY)
    @Schema(description = "PayPal货币类型")
    private String paypalCurrency;

    @ConfigItemField(PAYPAL_CLIENT_ID)
    @Schema(description = "PayPal客户端ID")
    private String paypalClientId;

    @ConfigItemField(PAYPAL_SECRET)
    @Schema(description = "PayPal密钥")
    private String paypalSecret;

    public void noUpdate(PayPalSettingsVO settings) {
        if (Objects.equals(this.paypalClientId, settings.getPaypalClientId())) {
            this.paypalClientId = null;
        }
        if (Objects.equals(this.paypalSecret, settings.getPaypalSecret())) {
            this.paypalSecret = null;
        }
    }

}
