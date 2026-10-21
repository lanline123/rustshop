package com.tigshop.bean.dto.content;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 视频号账号列表查询参数
 */
@Getter
@Setter
@Schema(description = "视频号账号列表查询参数")
public class WechatVideoAccountListDTO extends BasePage {
    @Schema(description = "视频号ID")
    private String finderId;

    @Schema(description = "关联开始时间，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss")
    private String relationTimeStart;

    @Schema(description = "关联结束时间，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss")
    private String relationTimeEnd;
}
