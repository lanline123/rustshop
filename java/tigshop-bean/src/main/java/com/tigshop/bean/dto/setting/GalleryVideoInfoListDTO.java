package com.tigshop.bean.dto.setting;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "视频相册信息查询参数")
public class GalleryVideoInfoListDTO extends BasePage {
    @Schema(description = "相册id")
    private Integer id;

    @Schema(description = "格式筛选，如mp4、webm")
    private String format;

    @Schema(description = "来源筛选：local=本地上传, external=外部直链")
    private String source;

}
