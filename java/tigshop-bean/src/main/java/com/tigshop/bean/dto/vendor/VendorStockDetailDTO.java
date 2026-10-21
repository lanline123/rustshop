package com.tigshop.bean.dto.vendor;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商库存出入库明细查询条件（按规格查库存日志）
 *
 * @author Tigshop团队
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorStockDetailDTO extends BasePage {

    @Schema(description = "供应商商品id（vendor_product.id）")
    private Integer productId;

    @Schema(description = "供应商商品规格id（vendor_product_sku.id）")
    private Long skuId;

    @Schema(description = "库存日志变动类型；0-全部，1-入库(增加)，2-出库(减少)")
    private Integer type;

    @Schema(description = "供应商id", hidden = true)
    private Integer vendorId;
}
