package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册信息")
public class GalleryVideoInfoDetailVO {

    @Schema(description = "视频相册信息id")
    private Integer id;

    @Schema(description = "视频相册分组id")
    private Integer galleryId;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频简介")
    private String description;

    @Schema(description = "视频格式")
    private String format;

    @Schema(description = "视频时长")
    private String duration;

    @Schema(description = "视频大小")
    private String size;

    @Schema(description = "更新时间")
    private Long addTime;


}
