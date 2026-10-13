package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取直播间分享二维码响应
 *
 * @author Tigshop团队
 * @create 2026年03月13日 16:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取直播间分享二维码响应")
public class GetSharedCodeResult extends WechatApiResult {

    @Schema(description = "分享二维码地址")
    private String cdnUrl;

    @Schema(description = "直播间页面路径")
    private String pagePath;

    @Schema(description = "分享海报地址")
    private String posterUrl;
}
