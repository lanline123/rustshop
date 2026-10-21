package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分组删除参数")
public class GalleryDelDTO {

    @NotNull(message = "分组ID不能为空")
    @Schema(description = "分组ID")
    private Integer id;

    @Schema(description = "删除模式：only_group(仅删分组)/group_and_files(删除分组和素材)")
    private String deleteMode;
}
