package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分组转移参数")
public class GalleryMoveDTO {

    @NotNull(message = "来源分组ID不能为空")
    @Schema(description = "来源分组ID")
    private Integer sourceId;

    @NotNull(message = "目标分组ID不能为空")
    @Schema(description = "目标分组ID，未分组为0")
    private Integer targetId;

    @NotNull(message = "转移方式不能为空")
    @Schema(description = "转移方式：move_and_delete/move_and_keep")
    private String mode;

    @NotNull(message = "类型不能为空")
    @Schema(description = "素材类型：image/video")
    private String type;
}
