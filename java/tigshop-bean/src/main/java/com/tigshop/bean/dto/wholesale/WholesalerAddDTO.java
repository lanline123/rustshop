package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 添加批发商入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "添加批发商入参")
public class WholesalerAddDTO {

    @NotNull(message = "userId 不能为空")
    @Positive(message = "userId 必须为正数")
    @Schema(description = "已存在的 user_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Integer userId;

    @NotNull(message = "gradeId 不能为空")
    @Positive(message = "gradeId 必须为正数")
    @Schema(description = "批发等级 ID（启用中）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer gradeId;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
