package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.YunPaySettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-云支付设置新增参数
 *
 * @author kidd
 * @since 2025/6/7 12:15
 */
@Data
public class YunPaySettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_YUNPAY)
    @Schema(description = "是否启用云支付；0-关闭，1-开启")
    private Integer useYunpay;

    @ConfigItemField(YUNPAY_UID)
    @Schema(description = "商户号")
    private String yunpayUid;

    @ConfigItemField(YUNPAY_SECRET_KEY)
    @Schema(description = "商户秘钥")
    private String yunpaySecretKey;

    public void noUpdate(YunPaySettingsVO settings) {
        if (Objects.equals(this.yunpayUid, settings.getYunpayUid())) {
            this.yunpayUid = null;
        }
        if (Objects.equals(this.yunpaySecretKey, settings.getYunpaySecretKey())) {
            this.yunpaySecretKey = null;
        }
    }

}
