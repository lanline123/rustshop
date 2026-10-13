package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品分享信息保存参数")
public class ProductShareInfoDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品图片")
    private String picUrl;
}
