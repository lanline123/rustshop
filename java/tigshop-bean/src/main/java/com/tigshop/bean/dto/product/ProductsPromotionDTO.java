// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import com.tigshop.bean.param.product.ProductSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 多个商品获得优惠和价格信息
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "多个商品获得优惠和价格信息")
public class ProductsPromotionDTO {

    @Schema(description = "商品列表")
    private List<ProductSaveParam> products;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "来源")
    private String promotionFrom;
}
