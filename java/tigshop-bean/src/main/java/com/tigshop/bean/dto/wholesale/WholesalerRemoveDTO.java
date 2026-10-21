package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 按 ID 直接移除批发商身份入参（CR-20260611-104500）。
 *
 * <p>与 {@link WholesalerRemoveByGradeDTO} 区别：本接口不需要 gradeId，按 wholesaler_id 精确移除，
 * 用于「直接删用户的批发商身份」通用场景。
 *
 * @author Tigshop团队
 * @create 2026-06-11
 */
@Data
@Schema(description = "按 ID 直接移除批发商身份入参")
public class WholesalerRemoveDTO {

    @Schema(description = "要移除的批发商 ID 列表（必填非空）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> wholesalerIds;
}
