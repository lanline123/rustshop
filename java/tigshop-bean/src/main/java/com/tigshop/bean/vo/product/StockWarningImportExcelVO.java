package com.tigshop.bean.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存预警导入行，同时用于生成导入模板
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警导入行")
public class StockWarningImportExcelVO {

    @ExcelProperty(value = "商品编码")
    @ColumnWidth(22)
    @Schema(description = "商品编码，可留空；商品编码和SKU编码至少填写一个")
    private String productSn;

    @ExcelProperty(value = "SKU编码")
    @ColumnWidth(22)
    @Schema(description = "SKU编码，多规格必填，单规格可留空；供应商按SKU编码导入")
    private String skuSn;

    /** 用字符串接收，填了非数字才能落到失败明细而不是整表读取失败 */
    @ExcelProperty(value = "下限")
    @ColumnWidth(12)
    @Schema(description = "库存下限")
    private String lowerLimit;

    @ExcelProperty(value = "上限")
    @ColumnWidth(12)
    @Schema(description = "库存上限")
    private String upperLimit;
}
