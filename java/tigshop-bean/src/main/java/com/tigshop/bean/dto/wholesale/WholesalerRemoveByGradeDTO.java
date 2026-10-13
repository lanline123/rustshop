package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 按等级移除批发商入参
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
@Data
@Schema(description = "按等级移除批发商入参")
public class WholesalerRemoveByGradeDTO {

    @NotNull(message = "gradeId 不能为空")
    @Schema(description = "批发等级 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer gradeId;

    @Schema(description = "指定要移除的批发商 ID（removeAll=true 时忽略）")
    private List<Integer> wholesalerIds;

    @Schema(description = "是否移除该等级下全部批发商，true 时忽略 wholesalerIds", example = "false")
    private Boolean removeAll = false;
}
