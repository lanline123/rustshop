package com.tigshop.bean.vo.product;

import com.tigshop.bean.model.product.ProductStockRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProductStockRecordVO {

    @Schema(description = "主键ID")
    private Integer productStockId;

    /**
     * 仓库编号
     */
    @Schema(description = "仓库编号")
    private String stockCode;

    /**
     * 11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库
     */
    @Schema(description = "11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库")
    private Integer type;

    /**
     * 经办人
     */
    @Schema(description = "经办人")
    private String addBy;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String comment;


    @Schema(description = "创建时间")
    private String addTime;


    @Schema(description = "出入库商品信息")
    private List<ProductStockRecordDetailVO> productList;
}
