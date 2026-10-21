package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品收藏
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商品收藏参数")
public class CollectProductSaveDTO {
    @Schema(description = "商品id")
    private Integer productId;
}
