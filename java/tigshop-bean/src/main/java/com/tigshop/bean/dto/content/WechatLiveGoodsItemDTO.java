package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 微信直播商品项DTO
 */
@Data
@Schema(description = "微信直播商品项DTO")
public class WechatLiveGoodsItemDTO {

    @Schema(description = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "商品SKU ID，0表示无规格")
    private Long productSkuId = 0L;
}
