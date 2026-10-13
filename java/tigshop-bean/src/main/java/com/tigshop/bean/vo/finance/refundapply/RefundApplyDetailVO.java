// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance.refundapply;

import com.tigshop.bean.enums.finance.RefundStatusEnum;
import com.tigshop.bean.enums.finance.RefundTypeEnum;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款申请VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款申请参数")
public class RefundApplyDetailVO {

    // *** RefundApply ***

    @Schema(description = "退款申请ID")
    private Integer refundId;

    @Schema(description = "退款类型：1:订单,2:商品")
    private Integer refundType;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款状态：0:未处理,1:处理中，2:已处理,3:取消,4:已拒绝")
    private Integer refundStatus;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "回复内容")
    private String refundNote;

    @Schema(description = "线上退款金额")
    private BigDecimal onlineBalance;

    @Schema(description = "线下退款金额")
    private BigDecimal offlineBalance;

    @Schema(description = "退款余额")
    private BigDecimal refundBalance;

    @Schema(description = "线上金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOnline;

    @Schema(description = "线下金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOffline;

    @Schema(description = "打款凭证")
    private String paymentVoucher;

    @Schema(description = "余额是否到账:0 无需处理1处理中 2已处理")
    private Integer isReceive;

    @Schema(description = "线上退款的关联ID")
    private Integer paylogRefundId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    // *** Order ***

    @Schema(description = "商品总金额")
    private BigDecimal productAmount;

    @Schema(description = "使用优惠券金额")
    private BigDecimal couponAmount;

    @Schema(description = "订单总金额（商品总价+运费等-优惠券-各种优惠活动）")
    private BigDecimal totalAmount;

    @Schema(description = "已支付金额(包含使用余额+线上支付金额+线下支付金额)")
    private BigDecimal paidAmount;

    @Schema(description = "未付款金额（total_amount - paid_amount）")
    private BigDecimal unpaidAmount;

    @Schema(description = "参加优惠活动金额，如折扣、满减等")
    private BigDecimal discountAmount;

    @Schema(description = "使用余额金额")
    private BigDecimal balance;

    @Schema(description = "线上支付金额（支付宝、微信等）")
    private BigDecimal onlinePaidAmount;

    @Schema(description = "线下支付金额（银行汇款等）")
    private BigDecimal offlinePaidAmount;

    @Schema(description = "订单号,唯一")
    private String orderSn;

    // *** OrderItem ***

    @Schema(description = "订单项")
    private List<RefundApplyDetailOrderItemVO> items;

    // *** Aftersales ***

    @Schema(description = "售后申请")
    private RefundApplyAftersalesVO aftersales;

    // *** Other ***

    @Schema(description = "退款类型")
    private String  refundTypeName;

    @Schema(description = "退款状态")
    private String  refundStatusName;

    @Schema(description = "真实的线上剩余可退金额")
    private BigDecimal effectiveOnlineBalance;

    @Schema(description = "已完成的总金额")
    private BigDecimal totalCompleteAmount;

    @Schema(description = "是否账期支付订单：0-否，1-是")
    private Integer isCreditOrder;

    @Schema(description = "账期支付金额（订单使用的账期额度）")
    private BigDecimal creditAmount;

    @Schema(description = "账期退款金额（本次售后应退的账期额度）")
    private BigDecimal creditRefundAmount;

    public RefundApplyDetailVO(RefundApply refundApply, Order order, List<OrderItem> orderItems, List<Product> products, List<ProductSku> productSkus, List<ECard> eCards, Aftersales aftersales, List<AftersalesItem> aftersalesItems, BigDecimal effectiveOnlineBalance, BigDecimal totalCompleteAmount, List<RefundApply> refundApplies, List<AftersalesItem> orderAllAftersalesItems) {
        this.refundId = refundApply.getRefundId();
        this.refundType = refundApply.getRefundType();
        this.orderId = refundApply.getOrderId();
        this.userId = refundApply.getUserId();
        this.aftersaleId = refundApply.getAftersaleId();
        this.refundStatus = refundApply.getRefundStatus();
        this.addTime = refundApply.getAddTime();
        this.refundNote = refundApply.getRefundNote();
        this.onlineBalance = refundApply.getOnlineBalance();
        this.offlineBalance = refundApply.getOfflineBalance();
        this.refundBalance = refundApply.getRefundBalance();
        this.isOnline = refundApply.getIsOnline();
        this.isOffline = refundApply.getIsOffline();
        this.paymentVoucher = refundApply.getPaymentVoucher();
        this.isReceive = refundApply.getIsReceive();
        this.paylogRefundId = refundApply.getPaylogRefundId();
        this.shopId = refundApply.getShopId();

        this.productAmount = order.getProductAmount();
        this.couponAmount = order.getCouponAmount();
        this.totalAmount = order.getTotalAmount();
        this.paidAmount = order.getPaidAmount();
        this.unpaidAmount = order.getUnpaidAmount();
        this.discountAmount = order.getDiscountAmount();
        this.balance = order.getBalance();
        this.onlinePaidAmount = order.getOnlinePaidAmount();
        this.offlinePaidAmount = order.getOfflinePaidAmount();
        this.orderSn = order.getOrderSn();

        this.items = orderItems.stream()
                .map(orderItem -> new RefundApplyDetailOrderItemVO(
                        orderItem,
                        aftersalesItems,
                        products,
                        productSkus,
                        eCards,
                        order.getTaxRate() != null ? order.getTaxRate() : BigDecimal.ZERO
                ))
                .toList();

        this.aftersales = new RefundApplyAftersalesVO(aftersales, order, aftersalesItems, orderItems, refundApplies, orderAllAftersalesItems);

        this.refundTypeName = RefundTypeEnum.getDescByCode(refundApply.getRefundType());
        this.refundStatusName = RefundStatusEnum.getDescByCode(refundApply.getRefundStatus());
        this.effectiveOnlineBalance = effectiveOnlineBalance;
        this.totalCompleteAmount = totalCompleteAmount;
    }
}
