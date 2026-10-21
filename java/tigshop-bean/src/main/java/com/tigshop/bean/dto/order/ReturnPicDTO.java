package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "图片参数")
public class ReturnPicDTO {
    @Schema(description = "图片名称")
    private String picName;

    @Schema(description = "图片缩略图")
    private String picThumb;

    @Schema(description = "图片URL")
    private String picUrl;
}
