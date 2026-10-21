package com.tigshop.bean.param.vendor.product;

import com.tigshop.bean.model.vendor.product.VendorProductVideo;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 供应商商品视频参数
 *
 * @author kidd
 * @since 2025/7/10 09:06
 */
@Data
public class VendorProductVideoParam {

    @NotBlank(message = "视频url不能为空")
    @Schema(description = "视频url")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @NotBlank(message = "视频格式后缀不能为空")
    @Schema(description = "视频格式后缀")
    private String format;

    public VendorProductVideo createVendorProductVideo(Long vendorProductId) {
        return VendorProductVideo.builder()
                .vendorProductId(vendorProductId)
                .videoUrl(this.getVideoUrl())
                .videoCover(this.getVideoCover())
                .format(this.getFormat())
                .vendorId(HeaderUtils.getVendorId())
                .build();
    }
}
