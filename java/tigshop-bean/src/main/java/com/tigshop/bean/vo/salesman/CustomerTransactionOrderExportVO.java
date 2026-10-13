// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.salesman;


import com.alibaba.excel.annotation.ExcelProperty;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/6/19 10:54
 */
@Data
@Schema(description = "成交客户导出")
public class CustomerTransactionOrderExportVO {

    @ExcelProperty(value = "订单编号", index = 0)
    @Schema(description = "订单编号")
    private String orderSn;

    @ExcelProperty(value = "下单时间", index = 1)
    @Schema(description = "下单时间")
    private String addTime;

    @ExcelProperty(value = "订单商品信息", index = 2)
    @Schema(description = "订单商品信息")
    private String productName;

    @ExcelProperty(value = "收货时间", index = 3)
    @Schema(description = "收货时间")
    private String receivedTime;

    @ExcelProperty(value = "所属分销员", index = 4)
    @Schema(description = "所属分销员")
    private String salesmanName;

    @ExcelProperty(value = "客户", index = 5)
    @Schema(description = "客户")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @ExcelProperty(value = "客户手机号", index = 6)
    @Schema(description = "客户手机号")
    private String userMobile;

    @ExcelProperty(value = "客户地址", index = 7)
    @Schema(description = "客户地址")
    private String userAddress;

    @ExcelProperty(value = "支付类型", index = 8)
    @Schema(description = "支付类型")
    //{{ order.payTypeId === 1 ? "在线支付" : order.payTypeId === 2 ? "货到付款" : "线下支付" }}
    private String payTypeStr;

    @ExcelProperty(value = "订单状态", index = 9)
    @Schema(description = "订单状态")
    private String orderStatusName;

    @ExcelProperty(value = "订单金额", index = 10)
    @Schema(description = "订单金额")
    private BigDecimal balance;

}
