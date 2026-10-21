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

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分商品VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "积分商品参数")
public class PointsExchangeVO {
    @Schema(description = "积分商品ID")
    private Integer id;

    @Schema(description = "商品ID")
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

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "商品图")
    private String picUrl;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "折后价格")
    private BigDecimal discountsPrice;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "虚拟销量")
    private Integer virtualSales;

    @Schema(description = "商品编号")
    private String productSn;

}