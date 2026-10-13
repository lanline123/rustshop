package com.tigshop.bean.feign.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信直播商品库新增商品返回
 */
@Data
@Schema(description = "微信直播商品库新增商品返回")
public class BroadcastGoodsAddResult {

    @Schema(description = "错误码")
    private Integer errcode;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "商品ID")
    @JsonProperty("goodsId")
    private Integer goodsId;

    @Schema(description = "审核单ID")
    @JsonProperty("auditId")
    private Long auditId;
}

