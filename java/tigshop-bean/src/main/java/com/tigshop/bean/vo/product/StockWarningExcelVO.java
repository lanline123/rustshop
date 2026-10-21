package com.tigshop.bean.vo.product;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存预警导出行
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警导出行")
public class StockWarningExcelVO {

    @ExcelProperty(value = "商品名称")
    @ColumnWidth(35)
    private String productName;

    @ExcelProperty(value = "规格")
    @ColumnWidth(25)
    private String productSkuName;

    @ExcelProperty(value = "商品编码")
    @ColumnWidth(20)
    private String productSn;

    @ExcelProperty(value = "SKU编码")
    @ColumnWidth(20)
    private String skuSn;

    @ExcelProperty(value = "仓库")
    @ColumnWidth(25)
    private String shopName;

    @ExcelProperty(value = "实时库存")
    @ColumnWidth(12)
    private Integer currentStock;

    @ExcelProperty(value = "在途库存")
    @ColumnWidth(12)
    private Integer transitStock;

    @ExcelProperty(value = "库存下限")
    @ColumnWidth(12)
    private String lowerLimit;

    @ExcelProperty(value = "库存上限")
    @ColumnWidth(12)
    private String upperLimit;

    @ExcelProperty(value = "库存状态")
    @ColumnWidth(12)
    private String stockStatusName;

    @ExcelProperty(value = "库存预警")
    @ColumnWidth(12)
    private String warnEnabledName;

    @ExcelProperty(value = "相关商品数")
    @ColumnWidth(12)
    private Integer relatedProductCount;
}
