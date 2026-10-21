package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "外部图片创建参数")
public class GalleryPicExternalDTO {

    @NotBlank(message = "图片地址不能为空")
    @Schema(description = "外部图片直链")
    private String picUrl;

    @Schema(description = "缩略图地址，默认与picUrl相同")
    private String picThumb;

    @Schema(description = "图片名称")
    private String picName;

    @NotNull(message = "分组ID不能为空")
    @Schema(description = "图片分组ID，未分组传0")
    private Integer galleryId;

    @NotBlank(message = "来源不能为空")
    @Schema(description = "来源，固定传external")
    private String source;
}
