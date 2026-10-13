package com.tigshop.bean.vo.vendor.product;

import com.tigshop.bean.model.vendor.product.VendorProductVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商商品视频
 *
 * @author kidd
 * @since 2025/7/10 10:55
 */
@Data
public class VendorProductVideoVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "视频url")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频格式后缀")
    private String format;

    public VendorProductVideoVO(VendorProductVideo video) {
        this.id = video.getId();
        this.videoUrl = video.getVideoUrl();
        this.videoCover = video.getVideoCover();
        this.format = video.getFormat();
    }
}
