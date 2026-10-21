package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 系统设置-订单设置
 *
 * @author kidd
 * @since 2025/6/6 13:35
 */
@Data
public class OrderSettingsVO {

    @ConfigItemField(SettingsEnum.AUTO_DELIVERY_DAYS)
    @Schema(description = "自动收货天数")
    private BigDecimal autoDeliveryDays;

    @ConfigItemField(SettingsEnum.AUTO_RETURN_GOODS)
    @Schema(description = "自动同意退货选项; 1-开启，0-关闭")
    private Integer autoReturnGoods;

    @ConfigItemField(SettingsEnum.AUTO_RETURN_GOODS_DAYS)
    @Schema(description = "自动同意退天数")
    private BigDecimal autoReturnGoodsDays;

    @ConfigItemField(SettingsEnum.AUTO_CONFIRM_RECEIPT_DAYS)
    @Schema(description = "买家退货多少天后，系统将自动确认收货")
    private BigDecimal autoConfirmReceiptDays;

    @ConfigItemField(SettingsEnum.AFTER_SALES_LIMIT_DAYS)
    @Schema(description = "售后限制天数")
    private BigDecimal afterSalesLimitDays;

    @ConfigItemField(SettingsEnum.AUTO_CANCEL_ORDER_MINUTE)
    @Schema(description = "未付款订单超时时间")
    private Integer autoCancelOrderMinute;

    @ConfigItemField(SettingsEnum.IS_PLATFORM_CANCEL_PAID_ORDER)
    @Schema(description = "平台已支付订单可取消订单")
    private Integer isPlatformCancelPaidOrder;

    @ConfigItemField(SettingsEnum.IS_PLATFORM_CANCEL_DELIVER_ORDER)
    @Schema(description = "平台已发货订单可取消发货")
    private Integer isPlatformCancelDeliverOrder;

    @ConfigItemField(SettingsEnum.IS_SHOP_CANCEL_DELIVER_ORDER)
    @Schema(description = "店铺已发货订单可取消发货")
    private Integer isShopCancelDeliverOrder;

    @ConfigItemField(SettingsEnum.AFTER_SALES_REVIEW)
    @Schema(description = "售后审核; 0-关闭，1-开启")
    private Integer afterSalesReview;

    @ConfigItemField(SettingsEnum.FINANCIAL_REVIEW)
    @Schema(description = "财务审核; 0-关闭，1-开启")
    private Integer financialReview;

    @ConfigItemField(SettingsEnum.VIRTUAL_ORDER_AUTO_DELIVER)
    @Schema(description = "虚拟商品自动发货; 0-关闭，1-开启")
    private Integer virtualOrderAutoDeliver;

    @ConfigItemField(SettingsEnum.AFTER_SALES_REASON_ENABLED)
    @Schema(description = "买家售后与取消订单原因开关; 0-关闭，1-开启")
    private Integer afterSalesReasonEnabled;

    @ConfigItemField(SettingsEnum.AFTER_SALES_REASON_LIST)
    @Schema(description = "买家售后与取消订单共用原因列表")
    private List<String> afterSalesReasonList;


    @ConfigItemField(SettingsEnum.ADMIN_SHOPPING_USE_POINT)
    @Schema(description = "(平台)代客下单使用积分（默认0小时）")
    private Integer adminShoppingUsePoint;

    @ConfigItemField(SettingsEnum.ADMIN_SHOPPING_USE_BALANCE)
    @Schema(description = "(平台)代客下单使用余额（默认0小时）")
    private Integer adminShoppingUseBalance;

}
