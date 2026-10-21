package com.tigshop.bean.dto.product;

import com.tigshop.bean.model.product.ProductStockRecordDetail;
import lombok.Data;

import java.util.List;

@Data
public class ProductStockInDTO {

    private Integer type;

    private String comment;

    private List<ProductStockRecordDetail> productStockRecordDetailList;

    private Integer productTotal;
}
