package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物配置
 *
 * @author kidd
 * @since 2025/4/3 16:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingSettingsVO {

    // *** 购物全局设置 ***

    @ConfigItemField(SettingsEnum.CHILD_AREA_NEED_REGION)
    @Schema(description = "运费模板地区设置机制；0-未设置的地区皆可配送（使用默认运费设置），1-仅设置的地区可配送")
    private Integer childAreaNeedRegion;

    // *** 积分设置 ***

    @ConfigItemField(SettingsEnum.INTEGRAL_NAME)
    @Schema(description = "积分名称")
    private String integralName;

    @ConfigItemField(SettingsEnum.INTEGRAL_SCALE)
    @Schema(description = "积分换算比例")
    private String integralScale;

    @ConfigItemField(SettingsEnum.ORDER_SEND_POINT)
    @Schema(description = "下单送积分")
    private String orderSendPoint;

    @ConfigItemField(SettingsEnum.ORDER_SEND_POINT_NODE)
    @Schema(description = "下单送积分节点；1-待发货，2-已发货，3-已完成")
    private Integer orderSendPointNode;

    @ConfigItemField(SettingsEnum.ORDER_COMPLETE_SEND_POINT_DAYS)
    @Schema(description = "会员订单已完成后多少天赠送积分；0表示立即赠送，大于0表示延迟N天后赠送")
    private Integer orderCompleteSendPointDays;

    @ConfigItemField(SettingsEnum.INTEGRAL_PERCENT)
    @Schema(description = "积分支付比例")
    private String integralPercent;

    @ConfigItemField(SettingsEnum.INTEGRAL_PERCENT_BASE_AMOUNT)
    @Schema(description = "积分支付比例金额基数")
    private String integralPercentBaseAmount;

    @ConfigItemField(SettingsEnum.COMMENT_SEND_POINT)
    @Schema(description = "评论商品送积分")
    private String commentSendPoint;

    @ConfigItemField(SettingsEnum.SHOW_SEND_POINT)
    @Schema(description = "晒单商品送积分")
    private String showSendPoint;

    @ConfigItemField(SettingsEnum.USE_QIANDAO_POINT)
    @Schema(description = "签到赠送积分; 0-否，1-是")
    private Integer useQiandaoPoint;

    @ConfigItemField(SettingsEnum.CONTINUOUS_SIGN_IN)
    @Schema(description = "连续签到；0-关闭，1-开启，漏签时重置签到天数")
    private Integer continuousSignIn;

    // *** 发票设置 ***

    @ConfigItemField(SettingsEnum.CAN_INVOICE)
    @Schema(description = "是否能开发票; 0-否，1-是")
    private Integer canInvoice;

    @ConfigItemField(SettingsEnum.INVOICE_ADDED)
    @Schema(description = "是否支持增值税专用发票; 0-否，1-是")
    private Integer invoiceAdded;

    // *** 退换货设置 ***

    @ConfigItemField(SettingsEnum.RETURN_CONSIGNEE)
    @Schema(description = "回寄联系人设置")
    private String returnConsignee;

    @ConfigItemField(SettingsEnum.RETURN_MOBILE)
    @Schema(description = "回寄电话设置")
    @Sensitive(type = SensitiveType.PHONE)
    private String returnMobile;

    @ConfigItemField(SettingsEnum.RETURN_ADDRESS)
    @Schema(description = "回寄地址设置")
    private String returnAddress;

}
