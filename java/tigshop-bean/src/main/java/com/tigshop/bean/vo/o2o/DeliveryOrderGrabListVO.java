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
 * 待抢单列表VO
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Data
@Schema(description = "待抢单列表VO")
public class DeliveryOrderGrabListVO {

    @Schema(description = "派送订单ID")
    private Integer deliveryOrderId;

    @Schema(description = "派送订单编号")
    private String deliveryOrderSn;

    @Schema(description = "关联商城订单编号")
    private String orderSn;

    @Schema(description = "类型：1-配送订单，2-服务订单")
    private Integer type;

    @Schema(description = "状态：10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "收货人/服务对象姓名")
    private String consignee;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "收货人/服务对象电话（部分隐藏）")
    private String mobile;

    @Schema(description = "配送地址/服务地址")
    private String address;

    @Schema(description = "配送地址/服务地址的地区名称")
    private String regionNames;

    @Schema(description = "取货地区名称")
    private String pickupRegionNames;

    @Schema(description = "取货地址（配送订单）")
    private String pickupAddress;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "取货电话")
    private String pickupPhone;

    @Schema(description = "取货地址纬度")
    private BigDecimal pickupLatitude;

    @Schema(description = "取货地址经度")
    private BigDecimal pickupLongitude;

    @Schema(description = "预约服务时间（服务订单）")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer serviceTime;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "买家备注")
    private String buyerNote;

    @Schema(description = "配送距离（取货点到配送地址，单位：米）")
    private BigDecimal distance;

    @Schema(description = "配送员到店铺的距离（单位：米）")
    private BigDecimal distanceToShop;

    @Schema(description = "配送员到配送地址的距离（单位：米）")
    private BigDecimal distanceToDelivery;

    @Schema(description = "创建时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer createTime;

    @Schema(description = "商品名称（逗号分隔）")
    private String productNames;

    @Schema(description = "预约时间")
    @TimestampFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Integer reservationTime;

    @Schema(description = "预计服务费（元），配送订单=配送服务费合计，服务订单=服务员服务费合计")
    private BigDecimal estimatedServiceFee;

    @Schema(description = "商品件数（订单行 quantity 合计）")
    private Integer goodsCount;

    @Schema(description = "商品明细列表")
    private List<DeliveryOrderDetailVO.OrderItemVO> orderItems;

    OrderReservation orderReservation;
}
