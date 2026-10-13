// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后商品VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "售后商品参数")
public class AftersalesItemVO {
    @Schema(description = "售后项目ID")
    private Integer aftersalesItemId;

    @Schema(description = "订单项ID")
    private Integer orderItemId;

    @Schema(description = "数量")
    private Integer number;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "订单号")
    private String orderSn;

    @JsonTranslate(dataType = 2)
    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "产品缩略图")
    private String picThumb;

    @Schema(description = "产品编号")
    private String productSn;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "供应商后台售后商品的下单供货价快照")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal supplyPrice;

    @Schema(description = "税率(百分比)")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRate;

    @Schema(description = "单个商品税费（按商品单价和税率计算，保留2位小数并向上取整）")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRateAmount;

    @Schema(description = "售后数量对应税费（taxRateAmount * number）")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxTotalAmount;

    @Schema(description = "附加规格数据")
    private String extraSkuData;

    @Schema(description = "规格数据")
    private List<ProductSkuDTO.SkuData> skuData;

    @Schema(description = "规格值")
    private String skuValue;
}
