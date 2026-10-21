package com.tigshop.bean.vo.product;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AllocateBatchImportErrorVO {

    @ExcelProperty(value = "出库门店", index = 0)
    @ColumnWidth(25)
    @Schema(description = "出库门店")
    public String stockOutOrg;

    @Schema(description = "入库门店")
    @ExcelProperty(value = "入库门店", index = 1)
    @ColumnWidth(25)
    public String stockInOrg;

    @Schema(description = "备注")
    @ExcelProperty(value = "备注", index = 2)
    @ColumnWidth(25)
    public String comment;

    @Schema(description = "商品名称")
    @ExcelProperty(value = "商品名称", index = 3)
    @ColumnWidth(25)
    public String productName;

    @Schema(description = "商品编码")
    @ExcelProperty(value = "商品编码", index = 4)
    @ColumnWidth(25)
    public String productSn;

    @Schema(description = "商品sku名称")
    @ExcelProperty(value = "商品规格名称", index = 5)
    @ColumnWidth(25)
    public String skuName;

    @Schema(description = "数量")
    @ExcelProperty(value = "数量", index = 6)
    @ColumnWidth(25)
    public String quantity;

    @Schema(description = "错误信息")
    @ExcelProperty(value = "导入失败信息", index = 7)
    private String errorMsg;
}
