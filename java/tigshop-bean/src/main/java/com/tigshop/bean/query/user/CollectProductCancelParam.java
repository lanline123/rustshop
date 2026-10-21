package com.tigshop.bean.query.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品收藏
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商品收藏参数")
public class CollectProductCancelParam {

    @NotNull(message = "商品主键不能为空")
    @Schema(description = "商品主键")
    private Integer id;

}
