// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.salesman;

import com.tigshop.bean.dto.product.ProductListDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分销商品列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "分销商品列表参数")
public class SalesmanProductPageQuery extends ProductListDTO {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "分销商品类型")
    private Integer salesmanProductType;

    @Schema(description = "是否加入")
    private Integer isJoin;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "店铺ID", hidden = true)
    private Integer shopId;

}
