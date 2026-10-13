package com.tigshop.bean.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频号账号返回
 */
@Data
@Schema(description = "视频号账号返回")
public class WechatVideoAccountVO {
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "视频号ID")
    private String finderId;

    @Schema(description = "关联时间")
    private String relationTime;
}
