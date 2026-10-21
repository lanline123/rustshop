package com.tigshop.bean.vo.order;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单导出参数")
public class OrderExportVO {
    @Schema(description = "自增ID")
    private Integer orderId;

    @ExcelProperty("订单编号")
    @ColumnWidth(25)
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

    @ExcelProperty("发货状态")
    @ColumnWidth(25)
    @Schema(description = "发货状态")
    private String shippingStatusName;

    @Schema(description = "商品配送情况;0未发货,1已发货,2已收货,4退货")
    private Integer shippingStatus;

    @ExcelProperty("支付状态")
    @ColumnWidth(25)
    @Schema(description = "支付状态")
    private String payStatusName;

    @Schema(description = "支付状态：0未支付 1支付中 2已支付 3退款中 4 已退款")
    private Integer payStatus;

    @ExcelProperty("下单时间")
    @ColumnWidth(25)
    @Schema(description = "订单生成时间")
    private String addTime;

    @ExcelProperty("收件人姓名")
    @ColumnWidth(25)
    @Schema(description = "收货人姓名")
    private String consignee;

    @ExcelProperty("收件人地址")
    @ColumnWidth(25)
    @Schema(description = "详细地址")
    private String address;

    @ExcelProperty("收件人邮编")
    @ColumnWidth(25)
    @Schema(description = "收货人的邮编")
    private String postcode;

    @Schema(description = "【JSON】地址id，number[]")
    private List<Integer> regionIds;

    @Schema(description = "【JSON】地址name, string[]")
    private List<String> regionNames;

    @Sensitive(type = SensitiveType.PHONE)
    @ExcelProperty("收件人电话")
    @ColumnWidth(25)
    @Schema(description = "收货人的手机")
    private String mobile;

    @ExcelProperty("订单备注")
    @ColumnWidth(25)
    @Schema(description = "订单备注")
    private String buyerNote;

    @ExcelProperty("商家备注")
    @ColumnWidth(25)
    @Schema(description = "商家备注")
    private String adminNote;

    @ExcelProperty("物流名称")
    @ColumnWidth(25)
    @Schema(description = "物流名称")
    private String logisticsName;

    @ExcelProperty("发货单号")
    @ColumnWidth(25)
    @Schema(description = "发货单号")
    private String trackingNo;

    @ExcelProperty("发货时间")
    @ColumnWidth(25)
    @Schema(description = "发货时间")
    private String shippingTime;

    @ExcelProperty("支付类型")
    @ColumnWidth(25)
    @Schema(description = "支付类型")
    private String payTypeName;

    @ExcelProperty("支付方式")
    @ColumnWidth(25)
    @Schema(description = "支付方式")
    private String payCode;

    @ExcelProperty("交易单号")
    @ColumnWidth(25)
    @Schema(description = "订单支付平台单号")
    private String outTradeNo;

    @ExcelProperty("支付时间")
    @ColumnWidth(25)
    @Schema(description = "订单支付时间")
    private String payTime;

    @ExcelProperty("店铺名称")
    @ColumnWidth(25)
    @Schema(description = "店铺名称")
    private String shopName;

    @ExcelProperty("订单总价")
    @ColumnWidth(25)
    @Schema(description = "订单总价")
    private BigDecimal totalAmount;

    @ExcelProperty("使用积分")
    @ColumnWidth(25)
    @Schema(description = "使用积分")
    private Integer usePoints;

    @ExcelProperty("优惠券金额")
    @ColumnWidth(25)
    @Schema(description = "优惠券金额")
    private BigDecimal couponAmount;

    @ExcelProperty("积分抵金额")
    @ColumnWidth(25)
    @Schema(description = "积分抵金额")
    private BigDecimal pointsAmount;

    @ExcelProperty("折扣")
    @ColumnWidth(25)
    @Schema(description = "折扣")
    private BigDecimal discountAmount;

    @ExcelProperty("使用余额")
    @ColumnWidth(25)
    @Schema(description = "使用余额")
    private BigDecimal balance;

    @Schema(description = "服务费")
    private BigDecimal serviceFee;

    @ExcelProperty("支付金额")
    @ColumnWidth(25)
    @Schema(description = "支付金额")
    private BigDecimal paidAmount;

    @ExcelProperty("运费")
    @ColumnWidth(25)
    @Schema(description = "运费")
    private BigDecimal shippingFee;

    @ExcelProperty("订单状态")
    @ColumnWidth(25)
    @Schema(description = "订单状态名称")
    private String orderStatusName;

    @ExcelProperty("会员名称")
    @ColumnWidth(25)
    @Schema(description = "用户名称")
    private String username;

//    @ExcelProperty("商品信息")
//    @ColumnWidth(25)
//    @Schema(description = "商品信息")
//    private String productInfo;

    @ExcelProperty("商品名称")
    @Schema(description = "商品名称")
    @ColumnWidth(25)
    private String productName;

    @ExcelProperty("商品规格")
    @ColumnWidth(25)
    @Schema(description = "商品规格")
    private String skuData;

    @ExcelProperty("商品编码")
    @ColumnWidth(25)
    @Schema(description = "商品编码")
    private String productSn;

    @ExcelProperty("商品数量")
    @ColumnWidth(25)
    @Schema(description = "商品数量")
    private Integer quantity;

    @ExcelProperty("售价")
    @ColumnWidth(25)
    @Schema(description = "售价")
    private BigDecimal price;

    @ExcelProperty("小计")
    @ColumnWidth(25)
    @Schema(description = "小计")
    private BigDecimal totalPrice;

    @ExcelProperty("总重量(KG)")
    @ColumnWidth(25)
    @Schema(description = "总重量")
    private BigDecimal productWeight;

}
