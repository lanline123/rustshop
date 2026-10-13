package com.tigshop.bean.model.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频相册
 * @author wzh
 */
@TableName(value ="gallery_video_info")
@Data
@Schema(description = "视频相册")
public class GalleryVideoInfo {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "相册id")
    private Integer galleryId;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频简介")
    private String description;

    @Schema(description = "视频格式")
    private String format;

    @Schema(description = "视频封面（第一帧）")
    private String videoFirstFrame;

    @Schema(description = "视频时长")
    private String duration;

    @Schema(description = "视频大小")
    private String size;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "操作人员id")
    private Integer addUserId;

    @Schema(description = "供应商id")
    private Integer vendorId;

    @Schema(description = "来源：upload=本地上传, external=外部直链")
    private String source;

}
