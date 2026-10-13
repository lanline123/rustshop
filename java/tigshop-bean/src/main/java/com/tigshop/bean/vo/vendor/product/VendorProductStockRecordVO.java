package com.tigshop.bean.vo.vendor.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 供应商出入库单（字段对齐店铺端 ProductStockRecordVO）
 *
 * @author Tigshop团队
 */
@Data
public class VendorProductStockRecordVO {

    @Schema(description = "出入库单id")
    private Integer productStockId;

    @Schema(description = "单据编号")
    private String stockCode;

    @Schema(description = "出入库子类型 11/12/10/21/22/23")
    private Integer type;

    @Schema(description = "经办人")
    private String addBy;

    @Schema(description = "备注")
    private String comment;

    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "出入库明细")
    private List<VendorProductStockRecordDetailVO> productList;
}
