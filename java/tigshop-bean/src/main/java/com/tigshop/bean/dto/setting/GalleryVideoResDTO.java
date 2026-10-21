package com.tigshop.bean.dto.setting;

import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 视频相册数据对象
 *
 * @author Jayce
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "视频相册数据对象")
public class GalleryVideoResDTO extends GalleryVideo {

    @Schema(description = "图片列表")
    private List<GalleryVideoInfo> galleryVideoInfoList;
}
