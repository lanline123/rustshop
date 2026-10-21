package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wzh
 */
@Data
@Schema(description = "上传视频信息")
@AllArgsConstructor
@NoArgsConstructor
public class UploadVideoInfoVO {

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;

    @Schema(description = "视频格式")
    private String format;

    @Schema(description = "视频封面")
    private String videoCover;
}
