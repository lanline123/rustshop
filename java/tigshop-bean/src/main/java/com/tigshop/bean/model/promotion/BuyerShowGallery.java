package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 买家秀相册
 * @author wzh
 * &#064;TableName  buyer_show_gallery
 */
@TableName(value ="buyer_show_gallery")
@Data
@Schema
public class BuyerShowGallery {

    @TableId(type = IdType.AUTO)
    @Schema(description = "相册ID")
    private Integer picId;

    @Schema(description = "买家秀id")
    private Integer buyerShowId;

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
}