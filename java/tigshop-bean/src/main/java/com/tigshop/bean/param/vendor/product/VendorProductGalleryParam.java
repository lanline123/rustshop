package com.tigshop.bean.param.vendor.product;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.model.vendor.product.VendorProductGallery;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 供应商商品相册参数
 *
 * @author kidd
 * @since 2025/7/10 09:01
 */
@Data
public class VendorProductGalleryParam {

    @Schema(description = "图片ID")
    private Long id;

    @NotBlank(message = "实际图片url不能为空")
    @Schema(description = "实际图片url")
    private String picUrl;

    @Schema(description = "图片说明信息")
    private String picDesc;

    @Schema(description = "微缩图片url")
    private String picThumb;

    @Schema(description = "原图url")
    private String picOriginal;

    @Schema(description = "高清图片url")
    private String picLarge;

    public VendorProductGallery createVendorProductGallery(Long vendorProductId, Integer sortOrder) {
        String originalPicUrl = StrUtil.isNotBlank(this.picOriginal) ? this.picOriginal : this.picUrl;
        VendorProductGallery vendorProductGallery = VendorProductGallery.builder()
                .vendorProductId(vendorProductId)
                .picDesc(this.picDesc)
                .picThumb(this.picThumb)
                .picOriginal(originalPicUrl)
                .sortOrder(sortOrder)
                .vendorId(HeaderUtils.getVendorId())
                .build();

        vendorProductGallery.setId(this.id);
        return vendorProductGallery;
    }
}
