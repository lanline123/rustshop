package com.tigshop.bean.vo.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.GalleryDTO;
import com.tigshop.bean.dto.setting.GalleryPicDTO;
import com.tigshop.bean.dto.setting.GalleryPicResDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "相册图片列表返回参数")
@Setter
@Getter
public class GalleryListResVO {
    @Schema(description = "子相册列表")
    private List<GalleryPicResDTO> childGalleryList;

    @Schema(description = "过滤结果")
    private Page<GalleryPicDTO> galleryPicPage;

    @Schema(description = "相册信息")
    private GalleryDTO galleryInfo;

    public GalleryListResVO(List<GalleryPicResDTO> childGalleryList, Page<GalleryPicDTO> galleryPicPage, GalleryDTO galleryInfo) {
        this.childGalleryList = childGalleryList;
        this.galleryPicPage = galleryPicPage;
        this.galleryInfo = galleryInfo;
    }
}
