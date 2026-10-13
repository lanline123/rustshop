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

import java.math.BigDecimal;

/**
 * 销售排行
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:38
 */
@Data
@Schema(description = "销售排行")
public class SalesRankingExportDTO {

    @ExcelProperty(value = "产品名称", index = 0)
    @Schema(description = "产品名称")
    private String productName;

    @ExcelProperty(value = "产品编号", index = 1)
    @Schema(description = "产品编号")
    private String productSn;

    @ExcelIgnore
    @Schema(description = "SKU数据")
    private String skuData;

    @ExcelProperty(value = "总销售数量", index = 2)
    @Schema(description = "总销售数量")
    private BigDecimal totalSalesNum;

    @ExcelProperty(value = "总销售额", index = 3)
    @Schema(description = "总销售额")
    private BigDecimal totalSalesAmount;

}
