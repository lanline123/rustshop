package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 买家秀视频
 * &#064;TableName  buyer_show_video
 * @author wzh
 */
@TableName(value ="buyer_show_video")
@Data
@Schema(description = "买家秀视频")
public class BuyerShowVideo {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "视频ID")
    private Integer videoId;

    @Schema(description = "买家秀d")
    private Integer buyerShowId;

    @Schema(description = "视频url")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频格式后缀")
    private String format;

    @Schema(description = "排序，与买家秀图片 sort_order 同一量纲，混排后第一条是视频则为视频类")
    private Integer sortOrder;
}