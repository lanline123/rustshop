package com.tigshop.bean.vo.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修分享信息")
public class DecorateShareInfoVO {

    @Schema(description = "装修ID")
    private Integer decorateId;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享描述")
    private String shareDesc;

    @Schema(description = "分享图片")
    private String shareImage;
}
