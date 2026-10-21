package com.tigshop.bean.dto.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "装修分享信息保存参数")
public class DecorateShareInfoDTO {

    @NotNull(message = "装修ID不能为空")
    @Schema(description = "装修ID")
    private Integer decorateId;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享描述")
    private String shareDesc;

    @Schema(description = "分享图片")
    private String shareImage;
}
