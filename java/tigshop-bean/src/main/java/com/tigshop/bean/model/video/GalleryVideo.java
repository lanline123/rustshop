package com.tigshop.bean.model.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频相册文件夹
 * @author wzh
 */
@TableName(value ="gallery_video")
@Data
@Schema(description = "视频分类（相册分组）")
public class GalleryVideo {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "商户id")
    private Integer shopId;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "创建人id")
    private Integer addUserId;

    @Schema(description = "供应商id")
    private Integer vendorId;
}