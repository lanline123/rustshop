package com.tigshop.bean.query.vendor;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商商品客户端列表
 *
 * @author kidd
 * @since 2025/7/16 13:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorProductClientPageQuery extends BasePage {

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "商品品牌ID")
    private Integer productBrandId;

    @Schema(description = "商品类目ID")
    private Integer productCategoryId;

    // *** Other ***

    @Schema(description = "是否可导入；0-否，1-是")
    private Integer isCanImport;
}
