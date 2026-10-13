package com.tigshop.bean.dto.product;

import com.tigshop.bean.model.product.ProductStockAllocateRecordDetail;
import lombok.Data;

import java.util.List;

@Data
public class AllocateStockOutDTO {
    private Integer allocateId;
    private String comment;
    private List<ProductStockAllocateRecordDetail> stockAllocateRecordDetails;
}
