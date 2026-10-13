package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分组排序参数")
public class GallerySortDTO {

    @NotEmpty(message = "排序ID列表不能为空")
    @Schema(description = "排序后的同级分组ID列表")
    private List<Integer> ids;

    @NotNull(message = "类型不能为空")
    @Schema(description = "素材类型：image/video")
    private String type;
}
