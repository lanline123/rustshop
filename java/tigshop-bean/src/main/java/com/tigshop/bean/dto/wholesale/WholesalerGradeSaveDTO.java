package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 批发等级新增/编辑入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发等级新增/编辑入参")
public class WholesalerGradeSaveDTO {

    @Schema(description = "不传=新增；>0=编辑")
    private Integer gradeId;

    @NotBlank(message = "等级名称不能为空")
    @Size(min = 1, max = 50, message = "等级名称长度 1-50")
    @Schema(description = "等级名称（唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "一级批发商")
    private String gradeName;

    @Schema(description = "排序值，默认50", example = "50")
    private Integer sortOrder;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
