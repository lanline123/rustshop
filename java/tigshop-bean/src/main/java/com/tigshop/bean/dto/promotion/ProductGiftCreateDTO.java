// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.promotion.ProductGiftConstants.*;

/**
 * 活动赠品创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "活动赠品参数")
public class ProductGiftCreateDTO {
    @Schema(description = "赠品名称")
    @NotBlank(message = GIFT_NAME_NOT_NULL)
    @Size(max = 100, message = GIFT_NAME_OVER_LENGTH)
    private String giftName;

    @Schema(description = "赠品库存")
    @Min(value = 1, message = GIFT_STOCK_NOT_NULL)
    private Integer giftStock;

    @Schema(description = "商品ID")
    @Min(value = 1, message = PRODUCT_ID_NOT_NULL)
    @NotNull(message = PRODUCT_ID_NOT_NULL)
    private Integer productId;

    @Schema(description = "规格ID")
    private Integer skuId;
}
