package com.tigshop.bean.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册上传信息")
public class GalleryVideoInfoUploadVO {

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;
}
