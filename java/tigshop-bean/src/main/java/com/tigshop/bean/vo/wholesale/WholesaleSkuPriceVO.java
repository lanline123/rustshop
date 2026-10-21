package com.tigshop.bean.vo.wholesale;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU 批发价 VO（含阶梯行）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "SKU 批发价")
public class WholesaleSkuPriceVO {

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "SKU 规格文本")
    private String skuSpecText;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "零售价（序列化为字符串）")
    private BigDecimal retailPrice;

    @Schema(description = "批发价行")
    private List<WholesalePriceRowVO> priceRows;
}
