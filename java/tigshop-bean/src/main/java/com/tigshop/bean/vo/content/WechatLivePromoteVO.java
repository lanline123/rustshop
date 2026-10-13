package com.tigshop.bean.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 直播间推广信息
 *
 * @author Tigshop团队
 * @create 2026年03月13日 16:20
 */
@Data
@Builder
@Schema(description = "直播间推广信息")
public class WechatLivePromoteVO {

    @Schema(description = "直播间房间号")
    private Integer roomId;

    @Schema(description = "直播间名称")
    private String roomName;

    @Schema(description = "直播间封面图")
    private String coverImg;

    @Schema(description = "直播间页面路径")
    private String pagePath;

    @Schema(description = "直播间二维码地址")
    private String qrcodeUrl;

    @Schema(description = "直播间海报地址")
    private String posterUrl;

    @Schema(description = "可复制进入直播间的微信短链")
    private String wechatShortLink;
}
