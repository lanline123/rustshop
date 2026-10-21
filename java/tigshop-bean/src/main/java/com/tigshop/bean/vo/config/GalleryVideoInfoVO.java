package com.tigshop.bean.vo.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.GalleryVideoResDTO;
import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册信息")
public class GalleryVideoInfoVO {
    @Schema(description = "子相册列表")
    private List<GalleryVideoResDTO> childGalleryList;

    @Schema(description = "过滤结果")
    private Page<GalleryVideoInfo> galleryVideoInfoPage;

    @Schema(description = "相册信息")
    private GalleryVideo galleryVideo;

    public GalleryVideoInfoVO(List<GalleryVideoResDTO> childGalleryList, Page<GalleryVideoInfo> galleryVideoInfoPage, GalleryVideo galleryVideo) {
        this.childGalleryList = childGalleryList;
        this.galleryVideoInfoPage = galleryVideoInfoPage;
        this.galleryVideo = galleryVideo;
    }

}
