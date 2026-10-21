package com.tigshop.bean.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存预警导入失败数据，用于导出修正后重新导入。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警导入失败数据")
public class StockWarningImportErrorExcelVO {

    @ExcelProperty(value = "商品编码", index = 0)
    @ColumnWidth(22)
    private String productSn;

    @ExcelProperty(value = "SKU编码", index = 1)
    @ColumnWidth(22)
    private String skuSn;

    @ExcelProperty(value = "下限", index = 2)
    @ColumnWidth(12)
    private String lowerLimit;

    @ExcelProperty(value = "上限", index = 3)
    @ColumnWidth(12)
    private String upperLimit;

    @ExcelProperty(value = "导入失败信息", index = 4)
    @ColumnWidth(36)
    @Schema(description = "导入失败信息")
    private String errorMsg;
}
