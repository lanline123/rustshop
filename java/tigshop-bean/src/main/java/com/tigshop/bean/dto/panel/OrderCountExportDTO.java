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

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单数
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:38
 */
@Data
@Schema(description = "订单数")
public class OrderCountExportDTO {

    @ExcelProperty(value = "时间", index = 0)
    @Schema(description = "时间")
    private String date;

    @ExcelProperty(value = "订单数", index = 1)
    @Schema(description = "订单数")
    private BigDecimal count;

}
