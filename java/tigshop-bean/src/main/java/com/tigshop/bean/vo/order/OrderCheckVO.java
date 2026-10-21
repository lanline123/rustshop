// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.finance.UserCreditInfoVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Tigshop
 */
@Data
@Schema(description = "订单检查信息")
public class OrderCheckVO {

//    @Schema(description = "地址列表")
//    private List<UserAddressVO> addressList;
//
//    @Schema(description = "可用支付方式")
//    private List<PaymentType> availablePaymentType;

//    @Schema(description = "店铺配送方式")
//    private List<List<ShippingType>> storeShippingType;

    @Schema(description = "购物车列表")
    private List<CartByStoreVO> cartList;

    @Schema(description = "总计信息")
    private Total total;

//    @Schema(description = "余额")
//    private BigDecimal balance;
//
//    @Schema(description = "积分")
//    private Integer points;

    @Schema(description = "当前环境是否有改价")
    private Boolean hasChangePrice;

    @Schema(description = "改价模式：1 整单改价，2 商品改价")
    private Integer changePriceType;

    @Schema(description = "整单改价类型：1 一口价，2 折扣，3 减少")
    private Integer priceChangeType;

    @Schema(description = "商品改价类型：1 一口价，2 折扣，3 减少")
    private Integer cartChangePriceType;

    @Schema(description = "可用积分")
    private Integer availablePoints;

    @Schema(description = "优惠券列表")
    private CouponList couponList;

    @Schema(description = "订单项信息")
    private Item item;

    @Schema(description = "使用的优惠券ID")
    private List<Integer> useCouponIds;

    @Schema(description = "选择的用户优惠券ID")
    private List<Integer> selectUserCouponIds;

    @Schema(description = "模板ID")
    private List<String> tmplIds;

    @Schema(description = "流程类型")
    private Integer flowType;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "商品配送方式")
    private DeliveryOptionVO deliveryOption;

    @Schema(description = "预售商品信息")
    private ProductPresaleVO productPresale;

    // *** 账期支付相关字段 ***
    @Schema(description = "账期支付信息（非授信会员时返回null）")
    private UserCreditInfoVO creditPaymentInfo;


    //预约上门是否超出了配送范围
    private Boolean isOutOfRangeForReservationHome;

    @Data
    @Schema(description = "支付方式")
    public static class PaymentType {
        @Schema(description = "支付类型ID")
        private Integer typeId;
        @Schema(description = "支付类型名称")
        private String typeName;
        @Schema(description = "是否禁用")
        private Boolean disabled;
        @Schema(description = "禁用描述")
        private String disabledDesc;
        @Schema(description = "是否显示")
        private Boolean isShow;
    }

    @Data
    @Schema(description = "配送方式")
    public static class ShippingType {
        @Schema(description = "配送方式ID")
        private Integer shippingTypeId;
        @Schema(description = "配送方式名称")
        @JsonTranslate
        private String shippingTypeName;
        @Schema(description = "原始配送方式名称")
        private String originShippingTypeName;
        @Schema(description = "默认配送方式ID")
        private Integer shippingDefaultId;
        @Schema(description = "配送方式描述")
        private String shippingTypeDesc;
        @Schema(description = "配送时间描述")
        private String shippingTimeDesc;
        @Schema(description = "是否支持货到付款")
        private Integer isSupportCod;
        @Schema(description = "排序号")
        private Integer sortOrder;
        @Schema(description = "店铺ID")
        private Integer shopId;
    }

    @Data
    @Schema(description = "总计信息")
    public static class Total {
        @Schema(description = "产品金额")
        private BigDecimal productAmount;
        @Schema(description = "选中数量")
        private Integer checkedCount;
        @Schema(description = "折扣")
        private BigDecimal discounts;
        @Schema(description = "折扣后金额")
        private String discountAfter;
        @Schema(description = "总数量")
        private Integer totalCount;
        @Schema(description = "优惠券折扣金额")
        private BigDecimal discountCouponAmount;
        @Schema(description = "折扣金额")
        private BigDecimal discountDiscountAmount;
        @Schema(description = "秒杀折扣金额")
        private BigDecimal discountSeckillAmount;
        @Schema(description = "产品促销折扣金额")
        private BigDecimal discountProductPromotionAmount;
        @Schema(description = "改价优惠")
        private BigDecimal discountChangePrice;
        @Schema(description = "限时折扣金额")
        private BigDecimal discountTimeDiscountAmount;
        @Schema(description = "服务费")
        private BigDecimal serviceFee;
        @Schema(description = "已付金额")
        private BigDecimal paidAmount;
        @Schema(description = "优惠券金额")
        private BigDecimal couponAmount;
        @Schema(description = "折扣金额")
        private BigDecimal discountAmount;
        @Schema(description = "兑换积分")
        private Integer exchangePoints;
        @Schema(description = "积分金额")
        private BigDecimal pointsAmount;
        @Schema(description = "运费")
        private BigDecimal shippingFee;
        @Schema(description = "店铺运费")
        private Map<Integer, BigDecimal> storeShippingFee;
        @Schema(description = "总金额")
        private BigDecimal totalAmount;
        @Schema(description = "订单发送积分")
        private Integer orderSendPoint;
        @Schema(description = "余额")
        private BigDecimal balance;
        @Schema(description = "未付金额")
        private BigDecimal unpaidAmount;
        @Schema(description = "账期抵扣金额")
        private BigDecimal creditAmount;
        @Schema(description = "礼品卡金额")
        private BigDecimal giftCardAmount;
        @Schema(description = "现金金额")
        private BigDecimal cashAmount;
        @Schema(description = "现金找零金额(现金金额(cashAmount)-支付的金额(amount))")
        private BigDecimal cashChangeAmount;
        @Schema(description = "抹分金额")
        private BigDecimal roundOffCentsAmount;
        @Schema(description = "收银台除去优惠卷和改价后的其他的优惠金额")
        private BigDecimal posOtherDiscountAmount;

        @Schema(description = "抹角金额")
        private BigDecimal roundOffDimeAmount;
        //每个店铺的运费规则
        @Schema(description = "店铺运费规则")
        private Map<Integer, Map<String, Object>> storeShippingRule;

        //预售模式下的定金金额
        @Schema(description = "定金金额（定金预售）")
        @JsonSerialize(using = BigDecimalSerializer.class)
        private BigDecimal depositAmount;

        @Schema(description = "实际支付的定金金额(depositAmount-余额)")
        @JsonSerialize(using = BigDecimalSerializer.class)
        private BigDecimal actualPayDepositAmount;

        @Schema(description = "尾款金额（定金预售）")
        @JsonSerialize(using = BigDecimalSerializer.class)
        private BigDecimal finalPaymentAmount;

        @Schema(description = "税率(百分比)")
        private BigDecimal taxRate;

        @Schema(description = "税率金额")
        private BigDecimal taxRateAmount;


        public BigDecimal getShippingFeeByShopId(Integer shopId) {
            if (Objects.isNull(this.storeShippingFee)) {
                return BigDecimal.ZERO;
            }
            return this.storeShippingFee.get(shopId);
        }
    }

    @Data
    @Schema(description = "运费")
    public static class ShippingFee {
        @Schema(description = "总数")
        private BigDecimal total;
        @Schema(description = "禁用的优惠券")
        private Map<Integer, BigDecimal> storeShippingFee;
    }

    @Data
    @Schema(description = "优惠券列表")
    public static class CouponList {
        @Schema(description = "启用的优惠券")
        private List<OrderCheckVO.Coupon> enableCoupons;
        @Schema(description = "禁用的优惠券")
        private List<OrderCheckVO.Coupon> disableCoupons;
    }

    @Data
    @Schema(description = "订单项信息")
    public static class Item {
        @Schema(description = "地址ID")
        private Integer addressId;
        @Schema(description = "配送方式")
        private Map<Integer, OrderCheckParam.ShippingType> shippingType;
        @Schema(description = "支付类型ID")
        private Integer payTypeId;
        @Schema(description = "使用积分")
        private Integer usePoint;
        @Schema(description = "使用余额")
        private BigDecimal useBalance;
        @Schema(description = "使用的优惠券ID")
        private List<Integer> useCouponIds;
        @Schema(description = "选择的用户优惠券ID")
        private List<Integer> selectUserCouponIds;
        @Schema(description = "流程类型")
        private Integer flowType;
        @Schema(description = "抹分金额")
        private Boolean roundOffCents;
        @Schema(description = "抹角金额")
        private Boolean roundOffDime;
        //deliveryType
        @Schema(description = "配送方式 1 物流配送，2 自提，3 同城配送")
        private Integer deliveryType;


        @Schema(description = "当前环境是否有改价")
        private Boolean hasChangePrice;

        @Schema(description = "改价模式：1 整单改价，2 商品改价")
        private Integer changePriceType;

        @Schema(description = "整单改价类型：1 一口价，2 折扣，3 减少")
        private Integer priceChangeType;

        @Schema(description = "商品改价类型：1 一口价，2 折扣，3 减少")
        private Integer cartChangePriceType;

        //       系统默认行为
        @Schema(description = "收款方式(支付方式) => 1. 现金支付 2 扫码支付 3 余额支付 4 积分支付")
        private Integer defaultCollectionMethod;
        @Schema(description = "使用积分")
        private Integer systemUsePoint;
        @Schema(description = "抹分金额")
        private Boolean systemRoundOffCents;
        @Schema(description = "抹角金额")
        private Boolean systemRoundOffDime;
    }

    @Data
    @Schema(description = "订单结算优惠券信息")
    public static class Coupon {

        @Schema(description = "优惠券ID")
        private Integer id;

        @Schema(description = "优惠券ID")
        private Integer couponId;
        @JsonTranslate(dataType = 10)
        @Schema(description = "优惠券名称")
        private String couponName;

        @Schema(description = "优惠券类型")
        private Integer couponType;

        @Schema(description = "优惠券面额")
        private BigDecimal couponMoney;

        @Schema(description = "优惠券最小订单金额")
        private BigDecimal minOrderAmount;

        @JsonTranslate(dataType = 10)
        @Schema(description = "优惠券描述")
        private String couponDesc;

        @Schema(description = "是否全局优惠券")
        private Boolean isGlobal;

        @Schema(description = "优惠券折扣")
        private BigDecimal couponDiscount;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "优惠券结束日期")
        private String endDate;

        @Schema(description = "用户优惠券ID")
        private Integer userCouponId;

        @Schema(description = "是否选择")
        private Boolean selected;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Coupon coupon = (Coupon) o;
            return Objects.equals(id, coupon.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }


}
