package com.tigshop.bean.vo.product;


import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockAllocateExcelVO {

    @ExcelProperty(value = "单据号")
    @ColumnWidth(25)
    @Schema(description = "单据号")
    private String allocateCode;

    @ExcelProperty(value = "出库组织")
    @ColumnWidth(25)
    @Schema(description = "出库组织")
    private String outboundOrganization;

    @ExcelProperty(value = "入库组织")
    @ColumnWidth(25)
    @Schema(description = "入库组织")
    private String warehousingOrganization;

    @ExcelProperty(value = "单据状态")
    @ColumnWidth(25)
    @Schema(description = "单据状态")
    private String status;

    @ExcelProperty(value = "经办人")
    @ColumnWidth(25)
    @Schema(description = "经办人")
    private String addBy;

    @ExcelProperty(value = "创建时间")
    @ColumnWidth(25)
    @Schema(description = "创建时间")
    private String addTime;


}
