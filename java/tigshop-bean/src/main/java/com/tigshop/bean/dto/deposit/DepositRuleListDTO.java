package com.tigshop.bean.dto.deposit;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺保证金规则列表查询参数。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺保证金规则列表查询参数")
public class DepositRuleListDTO extends BasePage {

    @Schema(description = "规则名称（模糊匹配）")
    private String ruleName;

    @Schema(description = "规则状态 1启用 0停用")
    private Integer status;

    @Schema(description = "生效时间筛选-开始（秒级时间戳）")
    private Long startTime;

    @Schema(description = "生效时间筛选-结束（秒级时间戳）")
    private Long endTime;
}
