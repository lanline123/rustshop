package com.tigshop.bean.dto.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "费率规则保存/编辑")
public class CategoryFeeRuleSaveDTO {

    @Schema(description = "规则ID，编辑时传")
    private Integer id;

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 50, message = "规则名称最多50个字符")
    private String ruleName;

    @NotNull(message = "适用对象类型不能为空")
    private Integer targetType;

    @NotNull(message = "范围类型不能为空")
    private Integer scopeType;

    @NotNull(message = "服务费率不能为空")
    @DecimalMin(value = "0.00", message = "服务费率不能小于0")
    @DecimalMax(value = "100.00", message = "服务费率不能大于100")
    private BigDecimal serviceFeeRate;

    @Size(max = 200, message = "备注最多200个字符")
    private String remark;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotEmpty(message = "关联类目不能为空")
    private List<Integer> categoryIds;

    @Schema(description = "关联对象ID：指定=包含；不包含=排除名单")
    private List<Integer> targetIds;
}
