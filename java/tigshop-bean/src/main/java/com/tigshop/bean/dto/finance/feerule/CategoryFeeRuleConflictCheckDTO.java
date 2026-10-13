package com.tigshop.bean.dto.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "费率规则冲突预检")
public class CategoryFeeRuleConflictCheckDTO {

    private Integer ruleId;

    @NotNull(message = "适用对象类型不能为空")
    private Integer targetType;

    @NotNull(message = "范围类型不能为空")
    private Integer scopeType;

    @NotEmpty(message = "关联类目不能为空")
    private List<Integer> categoryIds;

    private List<Integer> targetIds;
}
