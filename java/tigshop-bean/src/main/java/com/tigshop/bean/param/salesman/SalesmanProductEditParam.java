// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.salesman;

import com.tigshop.bean.model.salesman.SalesmanProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.tigshop.common.constant.salesman.SalesmanProductConstants.SALESMAN_PRODUCT_ID_NOT_NULL;

/**
  * 分销商品更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分销商品参数")
public class SalesmanProductEditParam extends SalesmanProductSaveParam {

    @NotNull(message = SALESMAN_PRODUCT_ID_NOT_NULL)
    @Schema(description = "分销商品ID")
    private Integer salesmanProductId;

    public SalesmanProduct updateSalesmanProduct(SalesmanProduct salesmanProduct) {
        SalesmanProduct updateSalesmanProduct = super.createSalesmanProduct();
        updateSalesmanProduct.setSalesmanProductId(salesmanProduct.getSalesmanProductId());

        return updateSalesmanProduct;
    }

}
