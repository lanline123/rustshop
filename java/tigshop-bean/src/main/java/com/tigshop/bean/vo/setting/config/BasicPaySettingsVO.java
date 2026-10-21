package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-支付基础设置
 *
 * @author kidd
 * @since 2025/6/7 10:43
 */
@Data
public class BasicPaySettingsVO {

    @ConfigItemField(USE_SURPLUS)
    @Schema(description = "是否启用余额支付；0-不支持，1-支持")
    private Integer useSurplus;

    @ConfigItemField(USE_POINTS)
    @Schema(description = "是否启用积分支付；0-不支持，1-支持")
    private Integer usePoints;

    @ConfigItemField(USE_COUPON)
    @Schema(description = "是否启用优惠券；0-不支持，1-支持")
    private Integer useCoupon;

    @ConfigItemField(USE_CREDIT)
    @Schema(description = "是否启用账期支付；0-不支持，1-支持")
    private Integer useCredit;

    @ConfigItemField(PAYMENT_ORDER)
    @Schema(description = "支付顺序，多个用逗号隔开，如：wechat,alipay,paypal,stripe")
    private String paymentOrder;

}
