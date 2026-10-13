package com.tigshop.bean.param.product;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.model.product.ProductStyle;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "商品样式创建参数")
@Data
public class ProductStyleSaveParam {

    @NotBlank(message = "样式名称不能为空")
    @Size(min = 1, max = 120, message = "样式名称长度不能超过120个字符")
    @Schema(description = "样式名称")
    private String styleName;

    @NotNull(message = "样式类型不能为空")
    @Schema(description = "样式类型 1=边框背景 2=活动背景")
    private Integer styleType;

    @NotBlank(message = "样式图片不能为空")
    @Schema(description = "样式图片URL")
    private String styleImage;

    @Schema(description = "PC端样式图片URL")
    private String pcImage;

    @Schema(description = "生效开始时间（活动背景必填，格式:yyyy-MM-dd HH:mm:ss）")
    private String startTime;

    @Schema(description = "生效结束时间（活动背景必填，格式:yyyy-MM-dd HH:mm:ss）")
    private String endTime;

    @Schema(description = "状态 1=启用 0=禁用")
    private Integer status;

    @Schema(description = "关联商品ID列表")
    private List<Integer> productIds;

    @Schema(description = "关联分组ID列表")
    private List<Integer> groupIds;

    @Schema(description = "关联品牌ID列表")
    private List<Integer> brandIds;

    public ProductStyle createProductStyle() {
        return ProductStyle.builder()
                .styleName(styleName)
                .styleType(styleType)
                .styleImage(styleImage)
                .pcImage(pcImage != null ? pcImage : "")
                .startTime(StrUtil.isNotBlank(startTime) ? TigUtils.toTimestampYmdHms(startTime) : 0L)
                .endTime(StrUtil.isNotBlank(endTime) ? TigUtils.toTimestampYmdHms(endTime) : 0L)
                .status(status)
                .isDelete(0)
                .addTime(System.currentTimeMillis() / 1000)
                .updateTime(System.currentTimeMillis() / 1000)
                .build();
    }
}
