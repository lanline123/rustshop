package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProductStockAllocateRecordDetailVO {

    @Schema(description = "调拨商品明细")
    private List<ProductAllocateDetailVO> productAllocateDetailVOList;
}
