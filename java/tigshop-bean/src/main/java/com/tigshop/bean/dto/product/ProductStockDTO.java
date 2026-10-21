package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockDTO extends BasePage {

    @Schema(description = "库存编码")
    private String stockCode;

    @Schema(description = "类型（1=入库，2=出库）")
    private Integer inOrOut;

    @Schema(description = "11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库")
    private Integer type;

    @Schema(description = "开始时间")
    private String beginTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "开始时间")
    private Long queryBeginTime;

    @Schema(description = "结束时间")
    private Long queryEndTime;

    @Schema(description = "出库组织id")
    private Integer shopId;

    @Schema(description = "入库组织id")
    private Integer targetShopId;

    @Schema(description = "商品类型(1=实物商品，2=虚拟商品，3=卡密内容，4=付费内容，5=预约上门，6=预约到店)")
    private Integer productType;

    @Schema(description = "库存搜索")
    private Integer stockSearch;
}
