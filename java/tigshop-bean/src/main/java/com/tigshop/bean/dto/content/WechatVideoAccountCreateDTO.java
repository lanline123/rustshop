package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.content.WechatVideoAccountConstants.*;

/**
 * 视频号账号创建参数
 */
@Data
@Schema(description = "视频号账号创建参数")
public class WechatVideoAccountCreateDTO {
    @NotBlank(message = VIDEO_ACCOUNT_FINDER_ID_NOT_BLANK)
    @Size(max = 64, message = VIDEO_ACCOUNT_FINDER_ID_MAX_LENGTH)
    @Schema(description = "视频号ID")
    private String finderId;
}
