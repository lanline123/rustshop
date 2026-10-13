// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.cart;

import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.enums.DataType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 添加购物车
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "购物车商品属性数据")
public class CarExtraSkuDataDTO {
    @Schema(description = "属性id")
    private Integer attributesId;

    @Schema(description = "属性名称")
    @JsonTranslate(dataType = 8)
    private String attrName;

    @Schema(description = "属性值")
    @JsonTranslate(dataType = 8)
    private String attrValue;

    @Schema(description = "属性价格")
    private BigDecimal attrPrice;

    @Schema(description = "总属性价格")
    private BigDecimal totalAttrPrice;
}
