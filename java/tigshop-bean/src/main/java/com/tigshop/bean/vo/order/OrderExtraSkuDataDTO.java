// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.order;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "商品属性数据")
public class OrderExtraSkuDataDTO {
    @Schema(description = "属性id")
    private Integer attributesId;

    @JsonTranslate(dataType = 1)
    @Schema(description = "属性名称")
    private String attrName;

    @JsonTranslate(dataType = 1)
    @Schema(description = "属性值")
    private String attrValue;

    @Schema(description = "属性价格")
    private BigDecimal attrPrice;

    @Schema(description = "属性价格")
    private BigDecimal totalAttrPrice;
}
