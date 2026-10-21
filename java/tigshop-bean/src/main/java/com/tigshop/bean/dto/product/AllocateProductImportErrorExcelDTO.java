package com.tigshop.bean.dto.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AllocateProductImportErrorExcelDTO {
    @Schema(description = "商品名称")
    @ExcelProperty(value = "商品名称")
    @ColumnWidth(25)
    private String productName;

    @Schema(description = "商品编码")
    @ExcelProperty(value = "商品编码")
    @ColumnWidth(25)
    private String productSn;

    @Schema(description = "商品规格名称")
    @ExcelProperty(value = "商品规格名称")
    @ColumnWidth(25)
    private String productSkuName;

    @Schema(description = "商品出入库数量")
    @ExcelProperty(value = "商品出入库数量")
    @ColumnWidth(25)
    private String quantity;

    @Schema(description = "错误信息")
    @ExcelProperty(value = "错误信息")
    @ColumnWidth(25)
    private String errorMsg;
}
