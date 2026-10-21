package com.tigshop.bean.param.settings.shippingtpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 模版信息参数
 *
 * @author kidd
 * @since 2025/4/22 10:38
 */
@Data
@Schema(description = "模版信息参数")
public class ShippingTplInfoParam {

    @Schema(description = "配送类型ID")
    private Long shippingTypeId;

    @Schema(description = "配送模板ID")
    private Long shippingTplId;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "起始数量")
    private BigDecimal startNumber;

    @Schema(description = "起始价格")
    private BigDecimal startPrice;

    @Schema(description = "增加数量")
    private BigDecimal addNumber;

    @Schema(description = "增加价格")
    private BigDecimal addPrice;

    @Schema(description = "免费价格")
    private BigDecimal freePrice;

    // *** Other ***

    @Schema(description = "区域数据")
    private RegionDataParam regionData;


}
