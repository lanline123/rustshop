package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 直播间列表和回放
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@Data
@Schema(description = "微信直播列表和回放")
public class GetLiveInfoParam {
    @Schema(description = "起始拉取视频，0表示从第一个视频片段开始拉取")
    private Integer start;

    @Schema(description = "每次拉取的个数，最大值100")
    private Integer limit;

    @Schema(description = "只能填\"get_replay\"，表示获取回放")
    private String action;

    @Schema(description = "直播间id")
    private Integer roomId;
}
