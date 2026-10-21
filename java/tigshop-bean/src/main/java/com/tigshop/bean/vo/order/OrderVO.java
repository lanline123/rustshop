package com.tigshop.bean.vo.order;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tigshop.bean.dto.content.WechatLiveContextDTO;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderPresale;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.promotion.card.GiftCardOrderVO;
import com.tigshop.bean.vo.setting.config.OrderSettingsVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 订单管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@AllArgsConstructor
@Schema(description = "订单管理参数")
public class OrderVO {

    // *** Order ***


    @Schema(description = "自增ID")
    private Integer orderId;

    @Schema(description = "订单类型")
    private Integer orderType;

    @Schema(description = "订单号,唯一")
    private String orderSn;

    @Schema(description = "用户id,同users的user_id")
    private Integer userId;

    @Schema(description = "父订单id")
    private Integer parentOrderId;

    @Schema(description = "父订单号")
    private String parentOrderSn;

    @Schema(description = "订单的状态：0 待确定，待支付 1 已确定，待发货 2处理中，已发货 3 已取消 4 无效 5 已完成")
    private Integer orderStatus;

    @Schema(description = "商品配送情况;0未发货,1已发货,2已收货,4退货")
    private Integer shippingStatus;

    @Schema(description = "支付状态：0未支付 1支付中 2已支付 3退款中 4 已退款")
    private Integer payStatus;

    @Schema(description = "收货人的姓名,用户页面填写,默认取值表user_address")
    private String consignee;

    @Schema(description = "详细地址")
    private String address;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "收货人的手机")
    private String mobile;

    @Schema(description = "收货人的Email")
    private String email;

    @Schema(description = "买家备注")
    private String buyerNote;

    @Schema(description = "商家备注，仅商家可见")
    private String adminNote;

    @Schema(description = "发货方式：1：物流，2：商家自提，3：无需配送")
    private Integer shippingMethod;

    @Schema(description = "快递公司id/为0则表示商家配送或无需配送")
    private Integer logisticsId;

    @Schema(description = "物流服务商：0历史未知或不适用，1快递鸟，2快递100")
    private Integer logisticsProvider;

    @Schema(description = "具体物流公司名称/商家配送/无需配送")
    private String logisticsName;

    @Schema(description = "配送类型id")
    private Integer shippingTypeId;

    @Schema(description = "发货单号")
    private String trackingNo;

    @Schema(description = "支付类型:1,在线支付（微信、支付等）;2,货到付款（支持先发货，后付款）;3,线下支付")
    private Integer payTypeId;

    @Schema(description = "使用的积分的数量")
    private Integer usePoints;

    @Schema(description = "是否需要分佣")
    private Integer isNeedCommisson;

    @Schema(description = "是否已分销分成")
    private Integer distributionStatus;

    @Schema(description = "推广人userId")
    private Integer referrerUserId;

    @Schema(description = "是否删除")
    private Integer isDel;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "是否已按店铺拆分:0,否;1,是")
    private Integer isStoreSplited;

    @Schema(description = "订单是否有效:1有效;0无效(支付后拆单父订单)")
    private Integer isValid;

    @Schema(description = "是否已评价")
    private Integer commentStatus;

    @Schema(description = "订单总金额（商品总价 + 运费等 - 优惠券 - 各种优惠活动）")
    private BigDecimal totalAmount;

    @Schema(description = "已支付金额(包含使用余额+线上支付金额+线下支付金额)")
    private BigDecimal paidAmount;

    @Schema(description = "未付款金额（计算方式为：total_amount - paid_amount）")
    private BigDecimal unpaidAmount;

    @Schema(description = "未退款金额（一般出现在订单取消或修改金额、商品后）")
    private BigDecimal unrefundAmount;

    @Schema(description = "商品的总金额")
    private BigDecimal productAmount;

    @Schema(description = "供应商订单供货价合计（商品供货价小计之和）")
    private BigDecimal supplyTotalAmount;

    @Schema(description = "使用优惠券金额")
    private BigDecimal couponAmount;

    @Schema(description = "使用积分金额")
    private BigDecimal pointsAmount;

    @Schema(description = "参加优惠活动金额，如折扣、满减等")
    private BigDecimal discountAmount;

    @Schema(description = "使用余额金额")
    private BigDecimal balance;

    @Schema(description = "账期支付金额")
    private BigDecimal creditAmount;

    @Schema(description = "是否账期订单：0-否，1-是")
    private Integer isCreditOrder;

    @Schema(description = "线上支付金额（支付宝、微信等）")
    private BigDecimal onlinePaidAmount;

    @Schema(description = "线下支付金额（银行汇款等）")
    private BigDecimal offlinePaidAmount;

    @Schema(description = "服务费")
    private BigDecimal serviceFee;

    @Schema(description = "配送费用")
    private BigDecimal shippingFee;

    @Schema(description = "发票费用")
    private BigDecimal invoiceFee;

    @Schema(description = "【JSON】记录订单使用的优惠券、优惠活动、不同店铺配送等的具体细节信息")
    private String orderExtension;

    @Schema(description = "下单来源设备，APP|PC|H5|微信公众号|微信小程序")
    private String orderSource;

    @Schema(description = "订单支付平台单号")
    private String outTradeNo;

    @Schema(description = "是否结算0不需要结算1未结算2已结算")
    private Integer isSettlement;

    @Schema(description = "是否是积分抵扣订单")
    private Integer isExchangeOrder;

    @Schema(description = "标记")
    private Integer mark;

    @Schema(description = "是否预售订单：0-否，1-是")
    private Integer isPresale;


    @Schema(description = "预售类型：1-全款预售，2-定金预售")
    private Integer presaleType;

    @Schema(description = "预售订单详细信息（仅预售订单有）")
    private OrderPresaleVO presaleInfo;

    @Schema(description = "是否微信直播订单")
    private Boolean wechatLiveOrder;

    @Schema(description = "渠道子来源")
    private String orderSubSource;

    @Schema(description = "微信直播上下文列表")
    private List<WechatLiveContextDTO> wechatLiveContexts;

    @Schema(description = "直播同步状态")
    private Integer liveSyncStatus;

    @Schema(description = "直播同步状态名称")
    private String liveSyncStatusName;

    @Schema(description = "直播同步结果说明")
    private String liveSyncMessage;

    // *** Other ***

    @Schema(description = "订单状态名称")
    @JsonTranslate
    private String orderStatusName;

    @Schema(description = "订单状态副文案（缺失时前端不展示副行；与 pc/uni 状态头文案同义）")
    private String orderStatusTip;

    @Schema(description = "支付截止时间 yyyy-MM-dd HH:mm:ss（仅待支付订单返回，用于买家端支付倒计时）")
    private String payEndTime;

    @Schema(description = "服务器当前时间 yyyy-MM-dd HH:mm:ss（用于买家端倒计时以服务端时间为准，规避本地时钟偏差）")
    private String serverTime;

    @Schema(description = "发货状态")
    private String shippingStatusName;

    @Schema(description = "支付状态")
    private String payStatusName;

    @Schema(description = "订单生成时间")
    private String addTime;

    @Schema(description = "【JSON】地址id，number[]")
    private List<Integer> regionIds;

    @Schema(description = "【JSON】地址name, string[]")
    private List<String> regionNames;

    @Schema(description = "【JSON】收货人信息")
    private UserAddressVO addressData;

    @Schema(description = "配送类型名称")
    @JsonTranslate
    private String shippingTypeName;

    @Schema(description = "订单配送时间")
    private String shippingTime;

    @Schema(description = "订单收货时间")
    private String receivedTime;

    @Schema(description = "订单支付时间")
    private String payTime;

    @Schema(description = "【JSON】发票信息")
    private OrderInvoiceVO invoiceData;

    @Schema(description = "地址")
    private String userAddress;

    @Schema(description = "用户数据")
    private UserBaseVO user;

    @Schema(description = "支付号")
    private String paySn;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "商品数据")
    private List<OrderItemVO> items;

    @Schema(description = "可操作项")
    private OrderActionVO availableActions;

    @Schema(description = "是否允许使用电子面单")
    private Boolean wayBill;

    @Schema(description = "进度")
    private OrderStepVO stepStatus;

    @Schema(description = "host")
    private String host;

    @Schema(description = "打印时间")
    private String printTime;

    @Schema(description = "是否可以售后；0-否，1-是")
    private Boolean toAftersalses;

    @Schema(description = "自动确认收货天数")
    private BigDecimal autoDeliveryDays;

    @Schema(description = "退货提示")
    private String returnGoodsTip;

    @Schema(description = "店铺信息")
    private ShopVO shop;

    @Schema(description = "订单流转设置；0-关闭，1-开启")
    private Integer isChangeOrderStatus;

    @Schema(description = "上级订单状态")
    private Integer preOrderStatus;

    @Schema(description = "上级订单状态描述")
    private String preOrderStatusDesc;

    @Schema(description = "供应商ID")
    private Integer vendorId;
    @Schema(description = "支付日志")
    private Paylog payLog;
    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "拼团状态desc")
    private String joinStatusDesc;

    @Schema(description = "拼团记录ID")
    private Long grouponPromotionRecordId;

    //预约上门
    @Schema(description = "是否是预约上门")
    private Boolean isReservationHome;

    @Schema(description = "是否自提")
    private Boolean isPickup;
    @Schema(description = "自提点信息")
    private PickupListVO pickupDetail;
    @Schema(description = "提货码")
    private String pickupCode;
    @Schema(description = "自提时间")
    private String expectPickupTime;
    @Schema(description = "提货提示")
    private String pickupTip;
    @Schema(description = "用户自提信息")
    private UserPickupInfo userPickupInfo;
    @Schema(description = "预约订单信息")
    private OrderReservationVO orderReservation;
    @Schema(description = "是否在售后中  false 不在，ture 在")
    private Boolean afterSalesIng;

    @Schema(description = "售后状态：0-未售后 1-售后中 2-售后完成")
    private Integer afterSalesStatus;

    @Schema(description = "订单卡")
    private GiftCardOrderVO giftCardOrder;

    @Schema(description = "改价优惠金额(改价优惠金额已经算入discountAmount里面,只是做回显)")
    private BigDecimal discountChangePrice;

    @Schema(description = "其他优惠金额(discountAmount-discountChangePrice)金额")
    private BigDecimal posOtherDiscountAmount;

    @Schema(description = "会员优惠金额（Σ(当前商品基础价-下单会员价)×数量，不含促销、优惠券和改价；仅支付详情返回）")
    private BigDecimal memberDiscountAmount;

    @Schema(description = "预计赠送积分数量（支付成功后按当前规则可获得的积分，仅支付详情返回）")
    private Integer willSendPoints;

    @Schema(description = "用户支付宝和微信支付的时候,通过当面付产品的时候用扫描枪扫描用户的付款码")
    private String authCode;

    @Schema(description = "取消订单原因")
    private String cancelRemark;

    @Schema(description = "抹分金额")
    private BigDecimal roundOffCentsAmount;

    @Schema(description = "抹角金额")
    private BigDecimal roundOffDimeAmount;

    @Schema(description = "税率(百分比)")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRate;

    @Schema(description = "税率金额")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRateAmount;

    // ============ 派送订单相关字段 ============

    @Schema(description = "派送订单信息（同城配送和预约上门订单有此信息）。美团运力时含 carrierType/mtStatus/mtStatusName；骑手姓名电话复用 staffName/staffPhone")
    private DeliveryOrderInfoVO deliveryOrderInfo;

    @Schema(description = "子单集合")
    private List<Integer> subOrderIds;

    public void assembleOrderSettings(OrderSettingsVO orderSettings) {
        this.isChangeOrderStatus = Constants.NO;

        if (RequestContextHolder.getRequestAttributes() == null) {
            return;
        }

        Integer shopId = HeaderUtils.getShopId();

        if (shopId != null && shopId == 0) {
            if (Constants.YES.equals(orderSettings.getIsPlatformCancelPaidOrder()) && OrderStatusEnum.CONFIRMED.getCode() == this.orderStatus) {
                this.isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(this.orderStatus);
                if (preStatus != null) {
                    this.preOrderStatus = preStatus.getCode();
                    this.preOrderStatusDesc = preStatus.getDescription();
                }
            }
            if (Constants.YES.equals(orderSettings.getIsPlatformCancelDeliverOrder()) && OrderStatusEnum.PROCESSING.getCode() == this.orderStatus) {
                this.isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(this.orderStatus);
                if (preStatus != null) {
                    this.preOrderStatus = preStatus.getCode();
                    this.preOrderStatusDesc = preStatus.getDescription();
                }
            }
        }

        if (shopId != null && shopId > 0) {
            if (Constants.YES.equals(orderSettings.getIsShopCancelDeliverOrder()) && OrderStatusEnum.PROCESSING.getCode() == this.orderStatus) {
                this.isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(this.orderStatus);
                if (preStatus != null) {
                    this.preOrderStatus = preStatus.getCode();
                    this.preOrderStatusDesc = preStatus.getDescription();
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "店铺VO")
    public static class ShopVO {

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "店铺标题")
        private String shopTitle;

        @Schema(description = "状态文本")
        private String statusText;

        @Schema(description = "客服入口信息页面")
        private String kefuInlet;

        @Schema(description = "店铺logo")
        private String shopLogo;

        public ShopVO(Shop shop) {
            this.shopId = shop.getShopId();
            this.shopTitle = shop.getShopTitle();
            this.statusText = ShopType.getTypeName(shop.getStatus());
            this.kefuInlet = shop.getKefuInlet();
            this.shopLogo = shop.getShopLogo();
        }
    }

    public OrderVO() {
        this.wayBill = false;
    }

    public OrderVO(Order order) {
        this.orderId = order.getOrderId();
        this.orderType = order.getOrderType();
        this.orderSn = order.getOrderSn();
        this.userId = order.getUserId();
        this.parentOrderId = order.getParentOrderId();
        this.parentOrderSn = order.getParentOrderSn();
        this.orderStatus = order.getOrderStatus();
        this.shippingStatus = order.getShippingStatus();
        this.payStatus = order.getPayStatus();
        this.consignee = order.getConsignee();
        this.address = order.getAddress();
        this.mobile = order.getMobile();
        this.email = order.getEmail();
        this.buyerNote = order.getBuyerNote();
        this.adminNote = order.getAdminNote();
        this.shippingMethod = order.getShippingMethod();
        this.logisticsId = order.getLogisticsId();
        this.logisticsProvider = order.getLogisticsProvider();
        this.logisticsName = order.getLogisticsName();
        this.shippingTypeId = order.getShippingTypeId();
        this.trackingNo = order.getTrackingNo();
        this.payTypeId = order.getPayTypeId();
        this.usePoints = order.getUsePoints();
        this.isNeedCommisson = order.getIsNeedCommisson();
        this.distributionStatus = order.getDistributionStatus();
        this.referrerUserId = order.getReferrerUserId();
        this.isDel = order.getIsDel();
        this.shopId = order.getShopId();
        this.isStoreSplited = order.getIsStoreSplited();
        this.isValid = order.getIsValid();
        this.commentStatus = order.getCommentStatus();
        this.totalAmount = order.getTotalAmount();
        this.paidAmount = order.getPaidAmount();
        this.unpaidAmount = order.getUnpaidAmount();
        this.unrefundAmount = order.getUnrefundAmount();
        this.productAmount = order.getProductAmount();
        this.couponAmount = order.getCouponAmount();
        this.pointsAmount = order.getPointsAmount();
        this.discountAmount = order.getDiscountAmount();
        this.balance = order.getBalance();
        this.creditAmount = order.getCreditAmount();
        this.isCreditOrder = order.getIsCreditOrder();
        this.onlinePaidAmount = order.getOnlinePaidAmount();
        this.offlinePaidAmount = order.getOfflinePaidAmount();
        this.serviceFee = order.getServiceFee();
        this.shippingFee = order.getShippingFee();
        this.invoiceFee = order.getInvoiceFee();
        this.orderExtension = order.getOrderExtension();
        this.orderSource = order.getOrderSource();
        this.outTradeNo = order.getOutTradeNo();
        this.isSettlement = order.getIsSettlement();
        this.isExchangeOrder = order.getIsExchangeOrder();
        this.mark = order.getMark();
        this.vendorId = order.getVendorId();
        this.cancelRemark = order.getCancelRemark();

        this.isPresale = order.getIsPresale();
        this.presaleType = order.getPresaleType();

        this.addressData = StringUtils.isNotBlank( order.getAddressData())? JSON.parseObject(order.getAddressData(), UserAddressVO.class) : null;

        boolean isCycleMainOrder = false;
        if (OrderType.isReservationOrder(order.getOrderType()) && !isCycleMainOrder) {
            this.orderStatusName = OrderReservationStatusEnum.getStatusName(order.getOrderStatus());
        } else if (Objects.equals(shippingTypeId, OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
            this.orderStatusName = OrderSameCityEnum.getStatusName(order.getOrderStatus());
        } else {
            this.orderStatusName = OrderStatusEnum.getStatusName(order.getOrderStatus());
        }

        this.shippingStatusName = ShippingStatusEnum.getStatusName(order.getShippingStatus());
        this.payStatusName = PaymentStatus.getStatusName(order.getPayStatus());
        this.addTime = TigUtils.handelTime(order.getAddTime());
        this.regionIds = JSON.parseArray(order.getRegionIds(), Integer.class);
        this.regionNames = JSON.parseArray(order.getRegionNames(), String.class);
        this.shippingTypeName = order.getShippingTypeName();
        this.shippingTime = order.getShippingTime() != null && order.getShippingTime() > 0 ? TigUtils.handelTime(order.getShippingTime()) : null;
        this.receivedTime = order.getReceivedTime() != null && order.getReceivedTime() > 0 ? TigUtils.handelTime(order.getReceivedTime()) : null;
        this.payTime = order.getPayTime() != null && order.getPayTime() > 0 ? TigUtils.handelTime(order.getPayTime()) : null;
        this.invoiceData = JSON.parseObject(order.getInvoiceData(), OrderInvoiceVO.class);
        if (regionNames != null) {
            this.userAddress = String.join(" ", regionNames) + " " + order.getAddress();
        }
        this.wayBill = true;
        this.discountChangePrice = order.getDiscountChangePrice();
        this.posOtherDiscountAmount = Optional.ofNullable(order.getDiscountAmount()).orElse(BigDecimal.ZERO).subtract(
                order.getDiscountChangePrice() != null ? order.getDiscountChangePrice() : BigDecimal.ZERO
        );
        this.roundOffCentsAmount = order.getRoundOffCentsAmount();
        this.roundOffDimeAmount = order.getRoundOffDimeAmount();
        this.taxRate = Optional.ofNullable(order.getTaxRate()).orElse(BigDecimal.ZERO);
        this.taxRateAmount = Optional.ofNullable(order.getTaxRateAmount()).orElse(BigDecimal.ZERO);
    }

    public void setUserAddressByRegionNames(List<String> regionNames){
        if (regionNames != null) {
            this.userAddress = String.join(" ", regionNames) + " " + this.getAddress();
        }
    }
}
