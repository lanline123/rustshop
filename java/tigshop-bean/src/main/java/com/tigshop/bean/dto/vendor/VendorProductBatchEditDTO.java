package com.tigshop.bean.dto.vendor;

import com.tigshop.bean.model.vendor.product.VendorProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 供应商商品批量修改 入参
 */
@Data
public class VendorProductBatchEditDTO {

    private List<EditItem> editItems;

    @Data
    public static class EditItem {
        @Schema(description = "供应商商品ID")
        private Long id;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "品牌ID")
        private Integer productBrandId;

        @Schema(description = "分类ID")
        private Integer productCategoryId;

        @Schema(description = "商品描述")
        private String productBrief;

        @Schema(description = "商品状态；0-断供，1-在售")
        private Integer productState;
    }

    public List<VendorProduct> buildVendorProducts() {
        return editItems.stream()
                .map(item -> {
                    VendorProduct vp = new VendorProduct();
                    vp.setId(item.getId());
                    vp.setProductName(item.getProductName());
                    vp.setProductBrandId(item.getProductBrandId());
                    vp.setProductCategoryId(item.getProductCategoryId());
                    vp.setProductBrief(item.getProductBrief());
                    vp.setProductState(item.getProductState());
                    return vp;
                })
                .toList();
    }
}
