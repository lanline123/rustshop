package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * im设置已读
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "im设置已读")
public class ImSetReadDTO {
    @Schema(description = "会话id")
    private Integer conversationId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "客服id")
    private Integer servantId;

    @Schema(description = "time")
    private String time;

}
