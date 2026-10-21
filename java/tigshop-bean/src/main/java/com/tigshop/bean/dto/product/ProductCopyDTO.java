package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 产品复制 入参
 *
 * @author kidd
 * @since 2025/3/26 09:10
 */
@Data
public class ProductCopyDTO {

    @Schema(description = "产品主键")
    private Integer productId;

    @Schema(description = "商品规格列表；复制编辑页可通过 isEnabled 指定复制后的 SKU 状态")
    private List<ProductListAttributeDTO> productList;
}
