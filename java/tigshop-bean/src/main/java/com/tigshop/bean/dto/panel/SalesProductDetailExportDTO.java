// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.panel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 销售明细看板
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:38
 */
@Data
@Schema(description = "销售明细看板")
public class SalesProductDetailExportDTO {

    @ExcelProperty(value = "产品名称", index = 0)
    @Schema(description = "产品名称")
    private String productName;

    @ExcelProperty(value = "产品编号", index = 1)
    @Schema(description = "产品编号")
    private String productSn;

//    @ExcelProperty(value = "SKU数据", index = 2)
    @ExcelIgnore
    @Schema(description = "SKU数据")
    private String skuData;

    @ExcelProperty(value = "订单编号", index = 2)
    @Schema(description = "订单编号")
    private String orderSn;

    @ExcelProperty(value = "数量", index = 3)
    @Schema(description = "数量")
    private Integer quantity;

    @ExcelProperty(value = "单价", index = 4)
    @Schema(description = "单价")
    private Double price;

    @ExcelProperty(value = "小计", index = 5)
    @Schema(description = "小计")
    private Double subtotal;

    @ExcelProperty(value = "下单时间", index = 6)
    @Schema(description = "下单时间")
    private String addTime;

}
