package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "统一分组树节点")
public class GalleryTreeNodeVO {

    @Schema(description = "分组ID")
    private Integer id;

    @Schema(description = "图片分组ID（兼容字段）")
    private Integer galleryId;

    @Schema(description = "父级ID")
    private Integer parentId;

    @Schema(description = "图片分组名称")
    private String galleryName;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "图片分组排序")
    private Integer gallerySort;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "素材数量")
    private Long picCount;

    @Schema(description = "子节点")
    private List<GalleryTreeNodeVO> children;
}
