package com.tigshop.bean.feign.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 视频号获取当前的直播记录返回
 *
 * @see <a href="https://developers.weixin.qq.com/doc/channels/api/channels/live/api_getfinderliverecordlist.html">微信文档</a>
 */
@Data
@Schema(description = "视频号获取当前的直播记录返回")
public class GetFinderLiveRecordListResult {
    @Schema(description = "错误码")
    private Integer errcode;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "直播记录列表")
    @JsonProperty("live_list")
    private List<LiveRecord> liveList;

    @Data
    @Schema(description = "直播记录")
    public static class LiveRecord {
        @Schema(description = "直播id")
        @JsonProperty("export_id")
        private String exportId;

        @Schema(description = "直播描述")
        private String description;

        @Schema(description = "直播封面")
        @JsonProperty("cover_url")
        private String coverUrl;

        @Schema(description = "开播视频号昵称")
        private String nickname;

        @Schema(description = "开播视频号头像")
        @JsonProperty("head_url")
        private String headUrl;
    }
}
