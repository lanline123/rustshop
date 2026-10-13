package com.tigshop.bean.vo.vendor.product;

import com.tigshop.bean.model.vendor.product.VendorProductGallery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商商品相册
 *
 * @author kidd
 * @since 2025/7/10 10:56
 */
@Data
public class VendorProductGalleryVO {

    @Schema(description = "主键")
    private Long id;

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

    @Schema(description = "排序")
    private Integer sortOrder;
    
    public VendorProductGalleryVO(VendorProductGallery gallery) {
        this.id = gallery.getId();
        this.picUrl = gallery.getPicUrl();
        this.picDesc = gallery.getPicDesc();
        this.picThumb = gallery.getPicThumb();
        this.picOriginal = gallery.getPicOriginal();
        this.picLarge = gallery.getPicLarge();
        this.sortOrder = gallery.getSortOrder();
    }
}
