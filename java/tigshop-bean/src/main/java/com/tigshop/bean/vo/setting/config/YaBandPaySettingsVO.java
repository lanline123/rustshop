package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-YaBandPay支付设置
 *
 * @author kidd
 * @since 2025/6/7 11:30
 */
@Data
public class YaBandPaySettingsVO {

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

    public void encryptData() {
        this.yabandpayUid = StringUtils.maskMiddleHalf(this.yabandpayUid);
        this.yabandpaySecretKey = StringUtils.maskMiddleHalf(this.yabandpaySecretKey);
    }

}
