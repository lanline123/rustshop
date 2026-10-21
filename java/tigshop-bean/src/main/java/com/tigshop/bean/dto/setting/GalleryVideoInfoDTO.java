package com.tigshop.bean.dto.setting;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tigshop.common.config.IntegerListFlexibleDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;


/**
 * @author wzh
 */
@Data
@Schema(description = "视频相册信息保存参数")
public class GalleryVideoInfoDTO {
    @Schema(description = "视频相册信息id")
    private Integer id;

    /**
     * 分组 id；查询语义见 list 接口。
     * 写入时 null / [] / [0] 表示不归组；兼容前端传 number 或 number[]。
     */
    @JsonDeserialize(using = IntegerListFlexibleDeserializer.class)
    @Schema(description = "视频相册分组id，支持 number 或 number[]；空/0 表示不归组")
    private List<Integer> galleryId;

    @NotBlank(message = "视频地址不能为空")
    @Schema(description = "视频地址")
    private String videoUrl;

    @NotBlank(message = "视频名称不能为空")
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

    @Schema(description = "来源：upload=本地上传, external=外部直链")
    private String source;

    /**
     * 归一化为落库用的单个 gallery_id：忽略 0，取最后一个有效分组；无有效分组则 0（不归组）。
     */
    public Integer resolveGalleryId() {
        if (galleryId == null || galleryId.isEmpty()) {
            return 0;
        }
        Integer lastReal = null;
        for (Integer id : galleryId) {
            if (id != null && id > 0) {
                lastReal = id;
            }
        }
        return lastReal != null ? lastReal : 0;
    }
}
