package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * 微信直播商品导入DTO
 */
@Data
@Schema(description = "微信直播商品导入DTO")
public class WechatLiveGoodsImportDTO {

    @Schema(description = "商品项列表，优先使用")
    @Valid
    private List<WechatLiveGoodsItemDTO> goodsItems;

    @Schema(description = "商品ID列表，兼容旧版接口，等价于 productSkuId=0")
    private List<Long> productIds;
}
