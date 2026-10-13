package com.tigshop.bean.vo.promotion.card;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GiftCardDetailVO {

    @Schema(description = "1-兑换卡 2-储值卡")
    private Integer groupType;

    @Schema(description = "1-电子卡 2-实体卡")
    private Integer cardType;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "卡密码")
    private String cardPassword;

    @Schema(description = "是否售出")
    private String isSold;

    @Schema(description = "是否兑换")
    private String isExchange;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "兑换信息")
    private ExchangeInfoVO exchangeInfo;

    @Data
    public static class ExchangeInfoVO{

        @Schema(description = "用户信息")
        private UserAddressInfoVO userAddressInfo;

        @Schema(description = "配送信息")
        private DeliveryInfoVO deliveryInfo;

        @Schema(description = "支付信息")
        private PayInfoVO payInfo;

    }

    @Data
    public static class UserAddressInfoVO{

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "收货人名称")
        private String consignee;

        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "手机")
        private String mobile;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "地区")
        private String regionName;

        @Schema(description = "用户地址")
        private String address;

        @Schema(description = "订单备注")
        private String buyerNote;
    }

    @Data
    public static class DeliveryInfoVO{

        @Schema(description = "配送方式")
        private String shippingTypeName;

        @Schema(description = "发货时间")
        private String shippingTime;

        @Schema(description = "物流名称")
        private String logisticsName;

        @Schema(description = "物流单号")
        private String trackingNo;
    }

    @Data
    public static class PayInfoVO{

        @Schema(description = "支付类型")
        private String payTypeName;

        @Schema(description = "兑换时间")
        private String exchangeTime;

        @Schema(description = "支付金额")
        private BigDecimal exchangeAmount;

        @Schema(description = "订单收货时间")
        private String receivedTime;
    }
}
