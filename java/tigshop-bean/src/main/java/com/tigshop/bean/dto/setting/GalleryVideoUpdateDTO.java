package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 视频相册更新数据对象
 *
 * @author Jayce
 */
@Data
@Schema(description = "视频相册更新数据对象")
public class GalleryVideoUpdateDTO {

    @NotNull(message = "主键id不能为空")
    @Schema(description = "主键id")
    private Integer id;

    @NotBlank(message = "相册名称不能为空")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序，不传则不修改")
    private Integer sort;
}
