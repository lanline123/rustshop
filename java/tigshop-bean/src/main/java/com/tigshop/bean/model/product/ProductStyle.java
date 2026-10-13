package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品样式配置表")
@TableName("product_style")
public class ProductStyle implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "样式ID")
    private Integer styleId;

    @Schema(description = "样式名称")
    @JsonTranslate(dataType = 18)
    private String styleName;

    @Schema(description = "样式类型 1=边框背景 2=活动背景")
    private Integer styleType;

    @Schema(description = "样式图片URL")
    @JsonTranslate(dataType = 19)
    private String styleImage;

    @Schema(description = "PC端样式图片URL")
    @JsonTranslate(dataType = 19)
    private String pcImage;

    @Schema(description = "生效开始时间")
    private Long startTime;

    @Schema(description = "生效结束时间")
    private Long endTime;

    @Schema(description = "状态 0=禁用 1=启用")
    private Integer status;

    @Schema(description = "店铺ID，0=平台")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "是否删除 0=否 1=是")
    private Integer isDelete;
}
