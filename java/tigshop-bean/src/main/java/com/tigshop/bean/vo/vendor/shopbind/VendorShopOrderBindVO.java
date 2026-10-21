// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor.shopbind;


import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/7/9 10:24
 */
@Data
@Schema(description = "关联店铺订单列表VO")
public class VendorShopOrderBindVO {

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "店铺id")
    private Integer shopId;




    @Schema(description = "订单类型")
    private Integer orderType;

    @Schema(description = "订单号,唯一")
    private String orderSn;

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

    @Schema(description = "订单生成时间")
    private Long addTime;

    @Schema(description = "收货人的姓名,用户页面填写,默认取值表user_address")
    private String consignee;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "地址id，number[]（JSON）")
    private String regionIds;

    @Schema(description = "地址name, string[]（JSON）")
    private String regionNames;

    @Schema(description = "收货人信息（JSON）")
    private String addressData;

    @Schema(description = "收货人的手机")
    @Sensitive(type = SensitiveType.PHONE)
    private String mobile;

    @Schema(description = "收货人的Email")
    private String email;

    @Schema(description = "买家备注")
    private String buyerNote;

    @Schema(description = "商家备注，仅商家可见")
    private String adminNote;

    @Schema(description = "发货方式：1物流，2商家自提，3无需配送")
    private Integer shippingMethod;

    @Schema(description = "快递公司id/为0则表示商家配送或无需配送")
    private Integer logisticsId;

    @Schema(description = "具体物流公司名称/商家配送/无需配送")
    private String logisticsName;

    @Schema(description = "配送类型id")
    private Integer shippingTypeId;

    @Schema(description = "配送类型名称")
    private String shippingTypeName;

    @Schema(description = "发货单号")
    private String trackingNo;

    @Schema(description = "订单配送时间")
    private Long shippingTime;

    @Schema(description = "订单收货时间")
    private Long receivedTime;

    @Schema(description = "支付类型:1在线支付;2货到付款;3线下支付")
    private Integer payTypeId;

    @Schema(description = "订单支付时间")
    private Long payTime;

    @Schema(description = "使用的积分数量")
    private Integer usePoints;

    @Schema(description = "是否需要分佣")
    private Integer isNeedCommisson;

    @Schema(description = "是否已分销分成")
    private Integer distributionStatus;

    @Schema(description = "推广人userId")
    private Integer referrerUserId;

    @Schema(description = "是否删除")
    private Integer isDel;

    @Schema(description = "是否已按店铺拆分:0否;1是")
    private Integer isStoreSplited;

    @Schema(description = "是否已评价")
    private Integer commentStatus;

    @Schema(description = "订单总金额（商品总价+运费等-优惠券-各种优惠活动）")
    private BigDecimal totalAmount;

    @Schema(description = "已支付金额(包含使用余额+线上支付金额+线下支付金额)")
    private BigDecimal paidAmount;

    @Schema(description = "未付款金额（total_amount - paid_amount）")
    private BigDecimal unpaidAmount;

    @Schema(description = "未退款金额（订单取消或修改金额、商品后）")
    private BigDecimal unrefundAmount;

    @Schema(description = "商品总金额")
    private BigDecimal productAmount;

    @Schema(description = "使用优惠券金额")
    private BigDecimal couponAmount;

    @Schema(description = "使用积分金额")
    private BigDecimal pointsAmount;

    @Schema(description = "参加优惠活动金额，如折扣、满减等")
    private BigDecimal discountAmount;

    @Schema(description = "使用余额金额")
    private BigDecimal balance;

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

    @Schema(description = "订单扩展信息（JSON）")
    private String orderExtension;

    @Schema(description = "下单来源设备，APP|PC|H5|微信公众号|微信小程序")
    private String orderSource;

    @Schema(description = "发票信息（JSON）")
    private String invoiceData;

    @Schema(description = "订单支付平台单号")
    private String outTradeNo;

    @Schema(description = "是否结算:0未结算,1已结算")
    private Integer isSettlement;

    @Schema(description = "是否是积分抵扣订单")
    private Integer isExchangeOrder;

    @Schema(description = "标记")
    private Integer mark;

    @Schema(description = "是否周期购订单：0-否，1-是")
    private Integer isCycleOrder;

    @Schema(description = "分销员id")
    private Integer salesmanId;

    @Schema(description = "分销员名称")
    private String salesmanName;

}
