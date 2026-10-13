// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 活动赠品VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "活动赠品参数")
public class ProductGiftVO {
    @Schema(description = "赠品ID")
    private Integer giftId;

    @Schema(description = "赠品名称")
    @JsonTranslate(dataType = 2)
    private String giftName;

    @Schema(description = "赠品库存")
    private Integer giftStock;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品信息")
    private Product productInfo;

    @Schema(description = "sku数据")
    private ProductSku skuInfo;
}