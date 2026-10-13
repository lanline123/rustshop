package com.tigshop.bean.dto.shop;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺商品分类表列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "店铺商品分类表列表参数")
public class ShopProductCategoryListDTO extends BasePage {
    @Schema(description = "父类id")
    private Integer parentId;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "shop_id")
    private Integer shopId;
}
