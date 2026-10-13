package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 视频相册创建数据对象
 *
 * @author Jayce
 */
@Data
@Schema(description = "视频相册创建数据对象")
public class GalleryVideoCreateDTO {

    @NotNull(message = "父级id不能为空")
    @Schema(description = "父级id")
    private Integer parentId;

    @NotBlank(message = "相册名称不能为空")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序（创建时由服务端按同级 max+1 赋值，可不传）")
    private Integer sort;

}
