package com.tigshop.bean.feign.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信素材上传返回
 */
@Data
@Schema(description = "微信素材上传返回")
public class WechatUploadMaterialResult {

    @Schema(description = "错误码")
    private Integer errcode;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "素材 media_id")
    @JsonProperty("media_id")
    private String mediaId;

    @Schema(description = "素材 URL（部分接口返回）")
    private String url;
}

