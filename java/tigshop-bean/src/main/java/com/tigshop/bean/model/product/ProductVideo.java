package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品视频
 * @author wzh
 */
@TableName(value ="product_video")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品视频")
public class ProductVideo {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "视频id")
    private Integer videoId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "视频url")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频格式")
    private String format;
}