package com.tigshop.bean.vo.product;

import com.tigshop.bean.model.shop.ShopFunds;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 配送类型VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
public class ProductShippingTplListVO {
    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "模板名称")
    private String shippingTplName;

    @Schema(description = "店铺信息")
    private ShopFunds shop;

    @Schema(description = "店铺id")
    private Integer shopId;
}
