package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册")
public class GalleryVideoVO {

    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;
}
