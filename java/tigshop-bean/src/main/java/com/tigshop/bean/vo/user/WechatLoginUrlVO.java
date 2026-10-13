package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信授权URL
 *
 * @author kidd
 * @since 2025/4/23 14:13
 */
@Schema(description = "微信授权URL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginUrlVO {

    @Schema(description = "url")
    private String url;

    @Schema(description = "ticket")
    private String ticket;

    public WechatLoginUrlVO(String url) {
        this.url = url;
    }
}
