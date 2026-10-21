package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import lombok.Data;

@Data
public class ProductStockDetailDTO extends BasePage {

    private Integer productId;

    private Integer skuId;


}
