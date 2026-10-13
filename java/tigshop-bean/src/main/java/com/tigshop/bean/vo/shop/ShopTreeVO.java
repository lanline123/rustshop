package com.tigshop.bean.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 店铺树VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺树VO")
public class ShopTreeVO {
    @Schema(description = "店铺树ID")
    private Integer categoryId;

    @Schema(description = "父级店铺树ID")
    private Integer parentId;

    @Schema(description = "店铺树名称")
    private String categoryName;

    @Schema(description = "子级店铺树")
    private List<ShopTreeVO> children;
}
