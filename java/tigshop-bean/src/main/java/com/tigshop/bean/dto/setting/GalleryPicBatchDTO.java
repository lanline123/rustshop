package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "素材批量操作参数")
public class GalleryPicBatchDTO {

    @NotEmpty(message = "素材ID列表不能为空")
    @Schema(description = "素材ID列表")
    private List<Integer> ids;

    @Schema(description = "素材类型：image/video")
    private String type;
}
