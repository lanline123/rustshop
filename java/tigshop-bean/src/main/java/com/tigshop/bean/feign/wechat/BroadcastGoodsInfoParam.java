package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信直播商品信息
 */
@Data
@Schema(description = "微信直播商品信息")
public class BroadcastGoodsInfoParam {

    @Schema(description = "商品封面图 mediaId")
    private String coverImgUrl;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "价格类型")
    private Integer priceType;

    @Schema(description = "价格")
    private Double price;

    @Schema(description = "商品小程序路径")
    private String url;
}

