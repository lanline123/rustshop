package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量移动素材参数")
public class GalleryPicTransferDTO {

    @NotEmpty(message = "素材ID列表不能为空")
    @Schema(description = "素材ID列表")
    private List<Integer> ids;

    @NotNull(message = "目标分组ID不能为空")
    @Schema(description = "目标分组ID，未分组为0")
    private Integer targetGalleryId;

    @NotNull(message = "类型不能为空")
    @Schema(description = "素材类型：image/video")
    private String type;
}
