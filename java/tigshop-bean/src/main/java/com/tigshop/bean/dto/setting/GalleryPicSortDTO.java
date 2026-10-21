package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "图片手动排序参数")
public class GalleryPicSortDTO {

    @NotNull(message = "分组ID不能为空")
    @Schema(description = "当前分组ID，未分组为0")
    private Integer galleryId;

    @NotEmpty(message = "排序ID列表不能为空")
    @Schema(description = "排序后的图片picId列表")
    private List<Integer> ids;
}
