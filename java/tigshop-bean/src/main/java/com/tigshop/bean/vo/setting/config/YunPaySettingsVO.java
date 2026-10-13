package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-云支付设置
 *
 * @author kidd
 * @since 2025/6/7 12:14
 */
@Data
public class YunPaySettingsVO {

    @ConfigItemField(USE_YUNPAY)
    @Schema(description = "是否启用云支付；0-关闭，1-开启")
    private Integer useYunpay;

    @ConfigItemField(YUNPAY_UID)
    @Schema(description = "商户号")
    private String yunpayUid;

    @ConfigItemField(YUNPAY_SECRET_KEY)
    @Schema(description = "商户秘钥")
    private String yunpaySecretKey;

    public void encryptData() {
        this.yunpayUid = StringUtils.maskMiddleHalf(this.yunpayUid);
        this.yunpaySecretKey = StringUtils.maskMiddleHalf(this.yunpaySecretKey);
    }

}
