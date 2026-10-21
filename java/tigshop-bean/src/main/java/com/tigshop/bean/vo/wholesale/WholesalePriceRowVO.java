package com.tigshop.bean.vo.wholesale;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批发价行 VO
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发价行")
public class WholesalePriceRowVO {

    @Schema(description = "等级ID：0=通用价")
    private Integer gradeId;

    @Schema(description = "等级名称")
    private String gradeName;

    @Schema(description = "阶梯起量")
    private Integer tierMinQty;

    @Schema(description = "价格模式：1=折扣 2=固定")
    private Integer priceMode;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "折率（序列化为字符串）")
    private BigDecimal discount;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "固定批发价（序列化为字符串）")
    private BigDecimal wholesalePrice;
}
