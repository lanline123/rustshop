package com.tigshop.bean.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductStockAllocateExcelExportDTO {

    private List<Integer> allocateIds;
    private Integer shopId;
}
