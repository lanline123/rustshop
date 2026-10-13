package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信直播商品库新增商品入参
 */
@Data
@Schema(description = "微信直播商品库新增商品入参")
public class BroadcastGoodsAddParam {

    @Schema(description = "商品信息")
    private BroadcastGoodsInfoParam goodsInfo;
}

