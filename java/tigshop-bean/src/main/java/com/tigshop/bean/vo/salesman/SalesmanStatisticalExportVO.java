package com.tigshop.bean.vo.salesman;

import com.alibaba.excel.annotation.ExcelProperty;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "分销员明细导出")
public class SalesmanStatisticalExportVO {
    @Sensitive(type = SensitiveType.PHONE)
    @ExcelProperty(value = "手机号", index = 0)
    @Schema(description = "手机号")
    private String mobile;

    @ExcelProperty(value = "昵称", index = 1)
    @Schema(description = "昵称")
    private String nickname;

    @ExcelProperty(value = "等级", index = 2)
    @Schema(description = "等级")
    private String levelText;

    @ExcelProperty(value = "分组名称", index = 3)
    @Schema(description = "分组名称")
    private String groupName;

    @ExcelProperty(value = "累计销售金额", index = 4)
    @Schema(description = "累计销售金额")
    private BigDecimal saleAmount;

    @ExcelProperty(value = "支付金额", index = 5)
    @Schema(description = "支付金额")
    private BigDecimal payMoney;

    @ExcelProperty(value = "退款金额", index = 6)
    @Schema(description = "退款金额")
    private BigDecimal refundMoney;

    @ExcelProperty(value = "支付订单数", index = 7)
    @Schema(description = "支付订单数")
    private Integer orderCount;

    @ExcelProperty(value = "退款订单数", index = 8)
    @Schema(description = "退款订单数")
    private Integer refundCount;

    @ExcelProperty(value = "已结算收益", index = 9)
    @Schema(description = "已结算收益")
    private BigDecimal totalCommission;

    @ExcelProperty(value = "累计收益", index = 10)
    @Schema(description = "累计收益")
    private BigDecimal totalAmountCommission;

    @ExcelProperty(value = "累计客户数", index = 11)
    @Schema(description = "累计客户数")
    private Long totalCustomer;
}
