package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新商品关键字
 *
 * @author Tigshop团队
 * @create 2024年12月04日 16:37
 */
@Data
@Schema(description = "更新商品关键字")
public class ProductKeyDTO {

    @Schema(description = "商品名称")
    private String productName;
}
