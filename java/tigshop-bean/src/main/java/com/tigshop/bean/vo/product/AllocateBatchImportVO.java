package com.tigshop.bean.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AllocateBatchImportVO {

    @ExcelProperty(value = "出库门店")
    @ColumnWidth(25)
    @Schema(description = "出库门店")
    public String stockOutOrg;

    @Schema(description = "入库门店")
    @ExcelProperty(value = "入库门店")
    @ColumnWidth(25)
    public String stockInOrg;

    @Schema(description = "备注")
    @ExcelProperty(value = "备注")
    @ColumnWidth(25)
    public String comment;

    @Schema(description = "商品名称")
    @ExcelProperty(value = "商品名称")
    @ColumnWidth(25)
    public String productName;

    @Schema(description = "商品编码")
    @ExcelProperty(value = "商品编码")
    @ColumnWidth(25)
    public String productSn;

    @Schema(description = "商品sku名称")
    @ExcelProperty(value = "商品规格名称")
    @ColumnWidth(25)
    public String skuName;

    @Schema(description = "数量")
    @ExcelProperty(value = "数量")
    @ColumnWidth(25)
    public String quantity;
}
