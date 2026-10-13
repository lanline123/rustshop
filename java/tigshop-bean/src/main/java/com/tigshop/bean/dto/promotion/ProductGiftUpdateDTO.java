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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.promotion.ProductGiftConstants.GIFT_STOCK_NOT_NULL;
import static com.tigshop.common.constant.promotion.ProductGiftConstants.PRODUCT_GIFT_ID_NOT_NULL;

/**
  * 活动赠品更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "活动赠品参数")
public class ProductGiftUpdateDTO {
    @Schema(description = "活动赠品ID")
    @NotNull(message = PRODUCT_GIFT_ID_NOT_NULL)
    private Integer giftId;

    @Schema(description = "赠品名称")
    private String giftName;

    @Schema(description = "赠品库存")
    @Min(value = 1, message = GIFT_STOCK_NOT_NULL)
    private Integer giftStock;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "规格ID")
    private Integer skuId;
}
