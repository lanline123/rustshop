package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "供应商商品相册")
@EqualsAndHashCode(callSuper = true)
@TableName(value ="vendor_product_gallery")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductGallery extends BasePO implements Serializable {

    @Schema(description = "供应商商品 ID")
    private Long vendorProductId;

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

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}