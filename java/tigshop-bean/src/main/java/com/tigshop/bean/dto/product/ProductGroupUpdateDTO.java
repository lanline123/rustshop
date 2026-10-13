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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.product.ProductGroupConstants.PRODUCT_GROUP_ID_NOT_NULL;

/**
 * 商品分组更新参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "商品分组参数")
public class ProductGroupUpdateDTO {
    @Schema(description = "商品分组ID")
    private Integer productGroupId;

    @Schema(description = "商品分组名称")
    private String productGroupName;

    @Schema(description = "商品分组编号")
    private String productGroupSn;

    @Schema(description = "商品分组描述")
    private String productGroupDescription;

    @Schema(description = "关联的商品ID列表")
    private List<Integer> productIds;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
