package com.tigshop.bean.vo.product;

import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.TimestampFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.checkerframework.checker.units.qual.Time;

@Schema(description = "商品样式列表VO")
@Data
public class ProductStyleVO {

    @Schema(description = "样式ID")
    private Integer styleId;

    @Schema(description = "样式名称")
    @JsonTranslate(dataType = 18)
    private String styleName;

    @Schema(description = "样式类型 1=边框背景 2=活动背景")
    private Integer styleType;

    @Schema(description = "样式类型名称")
    private String styleTypeName;

    @Schema(description = "样式图片URL")
    @JsonTranslate(dataType = 19)
    private String styleImage;

    @Schema(description = "PC端样式图片URL")
    @JsonTranslate(dataType = 19)
    private String pcImage;

    @Schema(description = "生效开始时间")
    @TimestampFormat
    private Long startTime;

    @Schema(description = "生效结束时间")
    @TimestampFormat
    private Long endTime;

    @Schema(description = "状态 0=禁用 1=启用")
    private Integer status;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "创建时间")
    @TimestampFormat
    private Long addTime;

    @Schema(description = "更新时间")
    @TimestampFormat
    private Long updateTime;

    @Schema(description = "关联商品数量")
    private Integer productCount;

    @Schema(description = "活动状态 1=活动未开始 2=活动进行中 3=活动已结束")
    private Integer activityStatus;

    @Schema(description = "活动状态名称")
    private String activityStatusName;
}
