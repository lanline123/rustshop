package com.tigshop.bean.vo.vendor.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商出入库批量导入失败数据
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class VendorImportErrorExcelVO {

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

    @Schema(description = "备注")
    @ExcelProperty(value = "备注")
    @ColumnWidth(25)
    private String comment;

    @Schema(description = "错误信息")
    @ExcelProperty(value = "错误信息")
    @ColumnWidth(25)
    private String errorMsg;
}
