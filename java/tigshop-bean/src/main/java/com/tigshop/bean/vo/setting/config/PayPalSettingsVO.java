package com.tigshop.bean.vo.setting.config;

import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-PayPal 支付设置
 *
 * @author kidd
 * @since 2025/6/7 11:52
 */
@Data
public class PayPalSettingsVO {

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

    @ConfigItemField(PAYPAL_CURRENCY_LIST)
    @Schema(description = "PayPal货币列表")
    private String paypalCurrencyListStr;

    // *** Other ***

    @Schema(description = "PayPal货币列表")
    private JSONArray paypalCurrencyList;

    public void encryptData() {
        this.paypalClientId = StringUtils.maskMiddleHalf(this.paypalClientId);
        this.paypalSecret = StringUtils.maskMiddleHalf(this.paypalSecret);
    }
}
