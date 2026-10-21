// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.model.order.OrderReservation;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.annotation.TimestampFormat;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 派送订单列表VO
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Data
@Schema(description = "派送订单列表VO")
public class DeliveryOrderVO {

    @Schema(description = "派送订单ID")
    private Integer deliveryOrderId;

    @Schema(description = "派送订单编号")
    private String deliveryOrderSn;

    @Schema(description = "关联商城订单ID")
    private Integer orderId;

    @Schema(description = "关联商城订单编号")
    private String orderSn;

    @Schema(description = "门店ID")
    private Integer shopId;

    @Schema(description = "门店名称")
    private String shopName;

    //增加门店地址和经纬度
    @Schema(description = "门店详细地址")
    private String shopDetailedAddress;

    @Schema(description = "店铺经度")
    private BigDecimal shopLongitude;

    @Schema(description = "店铺纬度")
    private BigDecimal shopLatitude;

    @Schema(description = "类型：1-配送订单，2-服务订单")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "派单模式：1-抢单模式，2-手动派单")
    private Integer dispatchMode;

    @Schema(description = "派单模式名称")
    private String dispatchModeName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "配送员/服务员ID")
    private Integer staffId;

    @Schema(description = "配送员/服务员姓名")
    private String staffName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "配送员/服务员电话")
    private String staffPhone;

    @Schema(description = "收货人/服务对象姓名")
    private String consignee;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "收货人/服务对象电话")
    private String mobile;

    @Schema(description = "配送地址/服务地址")
    private String address;

    @Schema(description = "配送地址/服务地址的地区名称")
    private String regionNames;

    @Schema(description = "配送地址和服务地址的纬度")
    private  BigDecimal latitude;

    @Schema(description = "配送地址和服务地址的纬度")
    private BigDecimal longitude;

    @Schema(description = "取货地区名称")
    private String pickupRegionNames;

    @Schema(description = "取货地址（门店地址）")
    private String pickupAddress;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "取货电话")
    private String pickupPhone;

    @Schema(description = "取货地址纬度")
    private BigDecimal pickupLatitude;

    @Schema(description = "取货地址经度")
    private BigDecimal pickupLongitude;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "创建时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer createTime;

    @Schema(description = "派单时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer dispatchTime;

    @Schema(description = "抢单时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer grabTime;

    @Schema(description = "开始服务时间/开始配送时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer pickupTime;

    @Schema(description = "预约时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer reservationTime;

    @Schema(description = "完成时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer completeTime;

    @Schema(description = "关联订单信息")
    private OrderInfoVO orderInfo;

    @Schema(description = "配送距离（取货点到配送地址，单位：米）")
    private BigDecimal distance;

    @Schema(description = "配送员到店铺的距离（单位：米）")
    private BigDecimal distanceToShop;

    @Schema(description = "配送员到配送地址的距离（单位：米）")
    private BigDecimal distanceToDelivery;

    @Schema(description = "预计服务费（元），配送订单=配送服务费合计，服务订单=服务员服务费合计")
    private BigDecimal estimatedServiceFee;

    @Schema(description = "商品件数（订单行 quantity 合计）")
    private Integer goodsCount;

    @Schema(description = "商品明细列表")
    private List<DeliveryOrderDetailVO.OrderItemVO> orderItems;

    @Schema(description = "预约时间段")
    private OrderReservation orderReservation;

    /**
     * 关联订单信息VO
     */
    @Data
    @Schema(description = "关联订单信息")
    public static class OrderInfoVO {
        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "订单编号")
        private String orderSn;

        @Schema(description = "收货人姓名")
        private String consignee;

        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "收货人手机")
        private String mobile;

        @Schema(description = "收货地址")
        private String address;

        @Schema(description = "商品数量（汇总）")
        private Integer productQuantity;

        @Schema(description = "商品总价")
        private BigDecimal productAmount;

        @Schema(description = "订单总金额（商品总价 + 运费等 - 优惠）")
        private BigDecimal totalAmount;

        @Schema(description = "实际付款金额")
        private BigDecimal paidAmount;

        @Schema(description = "配送费用")
        private BigDecimal shippingFee;

        @Schema(description = "订单状态")
        private Integer orderStatus;

        @Schema(description = "订单状态名称")
        private String orderStatusName;

        @Schema(description = "支付状态")
        private Integer payStatus;

        @Schema(description = "支付状态名称")
        private String payStatusName;

        @Schema(description = "买家备注")
        private String buyerNote;
    }
}
