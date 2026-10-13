// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量操作
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "批量操作")
public class UpdateItemDTO {

    @NotNull(message = "购物车ID不能为空")
    @Schema(description = "购物车ID")
    private Integer cartId;

    @Valid
    @Schema(description = "数据")
    private updateItemData data;

    @Data
    public static class updateItemData {

        @NotNull(message = "数量不能为空")
        @Schema(description = "数量")
        private Integer quantity;

    }
}
