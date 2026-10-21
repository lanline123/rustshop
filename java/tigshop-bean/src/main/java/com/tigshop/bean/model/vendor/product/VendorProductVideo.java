package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "供应商商品视频")
@EqualsAndHashCode(callSuper = true)
@TableName(value ="vendor_product_video")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductVideo extends BasePO implements Serializable {

    @Schema(description = "供应商商品 ID'")
    private Long vendorProductId;

    @Schema(description = "视频url")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频格式后缀")
    private String format;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}