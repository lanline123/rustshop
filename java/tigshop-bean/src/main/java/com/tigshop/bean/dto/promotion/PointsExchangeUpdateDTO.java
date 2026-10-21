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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

import static com.tigshop.common.constant.promotion.PointsExchangeConstants.POINTS_EXCHANGE_ID_NOT_NULL;
import static com.tigshop.common.constant.promotion.PointsExchangeConstants.PRODUCT_ID_NOT_NULL;

/**
  * 积分商品更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "积分商品参数")
public class PointsExchangeUpdateDTO {
    @Schema(description = "积分商品ID")
    @NotNull(message = POINTS_EXCHANGE_ID_NOT_NULL)
    private Integer id;

    @Schema(description = "商品ID")
    @NotNull(message = PRODUCT_ID_NOT_NULL)
    private Integer productId;

    @Schema(description = "所需积分")
    private Integer exchangeIntegral;

    @Schema(description = "积分抵扣金额")
    private BigDecimal pointsDeductedAmount;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "是否生效")
    private Integer isEnabled;

    @Schema(description = "属性ID")
    private Integer skuId;
}
