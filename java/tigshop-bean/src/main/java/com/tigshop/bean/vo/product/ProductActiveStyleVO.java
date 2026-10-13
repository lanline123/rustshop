package com.tigshop.bean.vo.product;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品生效样式VO（前台使用）")
@Data
public class ProductActiveStyleVO {

    @Schema(description = "边框样式ID")
    private Integer borderStyleId;

    @Schema(description = "边框图片URL")
    @JsonTranslate(dataType = 19)
    private String borderImage;

    @Schema(description = "边框H5图片URL")
    @JsonTranslate(dataType = 20)
    private String borderPcImage;

    @Schema(description = "活动背景样式ID")
    private Integer activityStyleId;

    @Schema(description = "活动背景样式名称")
    @JsonTranslate(dataType = 18)
    private String activityStyleName;

    @Schema(description = "活动背景图片URL")
    @JsonTranslate(dataType = 19)
    private String activityBackgroundImage;

    @Schema(description = "活动背景PC图片URL")
    @JsonTranslate(dataType = 20)
    private String activityBackgroundPcImage;
}
