package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册树形结构")
public class GalleryVideoListVO {

    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子集")
    private List<GalleryVideoListVO> children;
}
