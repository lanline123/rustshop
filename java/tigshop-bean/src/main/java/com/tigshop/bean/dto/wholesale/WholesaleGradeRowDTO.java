package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 批量设置类型售价的等级行（B7 gradeRows[] 元素）
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发价等级行")
public class WholesaleGradeRowDTO {

    @NotNull(message = "gradeId 不能为空")
    @Positive(message = "gradeId 必须为正数")
    @Schema(description = "启用中的等级 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer gradeId;

    @Valid
    @NotEmpty(message = "tiers 不能为空")
    @Schema(description = "该等级的阶梯行", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<WholesaleBatchTierDTO> tiers;
}
