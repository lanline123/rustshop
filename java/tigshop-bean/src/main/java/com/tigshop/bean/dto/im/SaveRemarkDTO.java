package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * im配置保类型
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "保存备注")
public class SaveRemarkDTO {
    @Schema(description = "会话id")
    private Integer conversationId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "会话总结")
    private String summary;

    @Schema(description = "店铺id")
    private Integer shopId;
}
