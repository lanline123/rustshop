package com.tigshop.bean.feign.wechat;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发货信息录入参数
 *
 * @author kidd
 * @since 2025/5/29 11:38
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发货信息录入参数")
public class UploadShippingInfoParam {

    @Schema(description = "订单，需要上传物流信息的订单")
    @JsonProperty("order_key")
    private OrderKey orderKey;

    @Schema(description = "物流模式; 1、实体物流配送采用快递公司进行实体物流配送形式 2、同城配送 3、虚拟商品，虚拟商品，例如话费充值，点卡等，无实体配送形式 4、用户自提")
    @JsonProperty("logistics_type")
    private Integer logisticsType;

    @Schema(description = "发货模式; 1、UNIFIED_DELIVERY（统一发货）2、SPLIT_DELIVERY（分拆发货）")
    @JsonProperty("delivery_mode")
    private Integer deliveryMode;

    @Schema(description = "分拆发货模式时必填，用于标识分拆发货模式下是否已全部发货完成，只有全部发货完成的情况下才会向用户推送发货完成通知。")
    @JsonProperty("is_all_delivered")
    private Boolean isAllDelivered;

    @Schema(description = "物流信息列表，发货物流单列表，支持统一发货（单个物流单）和分拆发货（多个物流单）两种模式，多重性: [1, 10]")
    @JsonProperty("shipping_list")
    private List<Shipping> shippingList;

    @Schema(description = "上传时间，用于标识请求的先后顺序 示例值: `2022-12-15T13:29:35.120+08:00`")
    @JsonProperty("upload_time")
    private String uploadTime;

    @Schema(description = "支付者，支付者信息")
    private Payer payer;

    @Schema(description = "订单信息")
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class OrderKey {

        @Schema(description = "订单单号类型，1，使用下单商户号和商户侧单号；2，使用微信支付单号")
        @JsonProperty("order_number_type")
        private Integer orderNumberType;

        @Schema(description = "原支付交易对应的微信订单号")
        @JsonProperty("transaction_id")
        private String transactionId;

        @Schema(description = "支付下单商户的商户号，由微信支付生成并下发")
        private String mchid;

        @Schema(description = "商户系统内部订单号")
        @JsonProperty("out_trade_no")
        private String outTradeNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "物流信息")
    public static class Shipping {

        @Schema(description = "物流单号，物流快递发货时必填，示例值: 323244567777 字符字节限制: [1, 128]")
        @JsonProperty("tracking_no")
        private String trackingNo;

        @Schema(description = "物流公司编码，快递公司ID，参见「获取运力 id 列表get_delivery_list」，物流快递发货时必填， 示例值: DHL 字符字节限制: [1, 128]")
        @JsonProperty("express_company")
        private String expressCompany;

        @Schema(description = "商品信息，例如：微信红包抱枕*1个，限120个字以内")
        @JsonProperty("item_desc")
        private String itemDesc;

        @Schema(description = "联系方式，当发货的物流公司为顺丰时，联系方式为必填，收件人或寄件人联系方式二选一")
        private Contact contact;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "联系方式")
        static class Contact {

            @Schema(description = "寄件人联系方式，寄件人联系方式，采用掩码传输，最后4位数字不能打掩码 示例值: `189****1234, 021-****1234, ****1234, 0**2-***1234, 0**2-******23-10, ****123-8008` 值限制: 0 ≤ value ≤ 1024")
            @JsonProperty("consignor_contact")
            private String consignorContact;

            @Schema(description = "收件人联系方式，收件人联系方式为，采用掩码传输，最后4位数字不能打掩码 示例值: `189****1234, 021-****1234, ****1234, 0**2-***1234, 0**2-******23-10, ****123-8008` 值限制: 0 ≤ value ≤ 1024")
            @JsonProperty("receiver_contact")
            private String receiverContact;
        }

        public void assembleContact(String consignorContact, String receiverContact) {
            consignorContact = StrUtil.isNotBlank(consignorContact) ? PhoneUtil.hideBetween(consignorContact).toString() : null;
            receiverContact = StrUtil.isNotBlank(receiverContact) ? PhoneUtil.hideBetween(receiverContact).toString() : null;
            this.contact = new Contact(consignorContact, receiverContact);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "支付者信息")
    public static class Payer {

        @Schema(description = "用户标识，用户在小程序appid下的唯一标识。")
        private String openid;
    }

}
