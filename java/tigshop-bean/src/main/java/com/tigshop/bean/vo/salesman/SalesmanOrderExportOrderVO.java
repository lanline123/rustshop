package com.tigshop.bean.vo.salesman;

import com.alibaba.excel.annotation.ExcelProperty;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存日志VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单维度计算报表")
public class SalesmanOrderExportOrderVO {
    @ExcelProperty(value = "添加时间", index = 0)
    @Schema(description = "添加时间")
    private String addTime;

    @ExcelProperty(value = "支付时间", index = 1)
    @Schema(description = "支付时间")
    private String payTime;

    @ExcelProperty(value = "分销员加入时间", index = 2)
    @Schema(description = "分销员加入时间")
    private String distributionRegisterTime;

    @ExcelProperty(value = "客户昵称", index = 3)
    @Schema(description = "客户昵称")
    private String customerNickname;

    @Sensitive(type = SensitiveType.PHONE)
    @ExcelProperty(value = "客户手机号", index = 4)
    @Schema(description = "客户手机号")
    private String customerMobile;

    @ExcelProperty(value = "所属门店", index = 5)
    @Schema(description = "门店信息")
    private String shopTitle;

    @ExcelProperty(value = "所属分组", index = 6)
    @Schema(description = "所属分组")
    private String groupName;

    @ExcelProperty(value = "订单号", index = 7)
    @Schema(description = "订单号")
    private String orderSn;

    @ExcelProperty(value = "成交金额", index = 8)
    @Schema(description = "成交金额")
    private BigDecimal totalProductMoney;

    @ExcelProperty(value = "订单佣金类型", index = 9)
    @Schema(description = "订单佣金类型")
    private String profitComposition;

    @ExcelProperty(value = "订单佣金", index = 10)
    @Schema(description = "订单佣金")
    private BigDecimal amount;

    @ExcelProperty(value = "结算状态", index = 11)
    @Schema(description = "结算状态")
    private String status;

    @ExcelProperty(value = "结算时间", index = 12)
    @Schema(description = "结算时间")
    private String settlementTime;

    @ExcelProperty(value = "结算方式", index = 13)
    @Schema(description = "结算方式")
    private String settlementType;

    @ExcelProperty(value = "邀请方昵称", index = 14)
    @Schema(description = "邀请方昵称")
    private String inviterNickname;

    @Sensitive(type = SensitiveType.PHONE)
    @ExcelProperty(value = "邀请方手机号", index = 15)
    @Schema(description = "邀请方手机号")
    private String inviterMobile;

    @ExcelProperty(value = "订单来源", index = 16)
    @Schema(description = "订单来源")
    private String orderSource;

    @ExcelProperty(value = "订单状态", index = 17)
    @Schema(description = "订单状态")
    private String orderStatus;

    @ExcelProperty(value = "退款金额", index = 18)
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @ExcelProperty(value = "退款时间", index = 19)
    @Schema(description = "退款时间")
    private String refundTime;

}
