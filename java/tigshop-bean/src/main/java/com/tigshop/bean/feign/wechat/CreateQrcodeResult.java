package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生成微信二维码
 *
 * @author kidd
 * @since 2025/4/23 15:36
 */
@Schema(description = "生成微信二维码")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateQrcodeResult extends WechatApiResult {

    @Schema(description = "获取的二维码ticket，凭借此ticket可以在有效时间内换取二维码")
    private String ticket;

    @Schema(description = "该二维码有效时间，以秒为单位。 最大不超过2592000（即30天）。")
    private String url;

    @Schema(description = "二维码图片解析后的地址，开发者可根据该地址自行生成需要的二维码图片")
    private Integer expireSeconds;
}
