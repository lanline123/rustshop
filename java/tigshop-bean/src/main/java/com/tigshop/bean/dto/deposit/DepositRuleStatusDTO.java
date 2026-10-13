package com.tigshop.bean.dto.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 店铺保证金规则启用/停用参数。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金规则状态参数")
public class DepositRuleStatusDTO {

    @Schema(description = "规则ID")
    @NotNull(message = "规则ID不能为空")
    private Integer shopDepositRuleId;

    @Schema(description = "状态 1启用 0停用")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
