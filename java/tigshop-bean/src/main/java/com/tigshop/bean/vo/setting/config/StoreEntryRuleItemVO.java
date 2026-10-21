package com.tigshop.bean.vo.setting.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首次进店规则项（「按规则进店」时的单条规则）
 *
 * @author Tigshop项目组
 */
@Schema(description = "首次进店规则项")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreEntryRuleItemVO {

    @Schema(description = "规则类型；1-进推广店铺，2-总部进店同城最近门店，3-无法识别进最近门店")
    private Integer ruleType;

    @Schema(description = "是否启用；0-否，1-是")
    private Integer enabled;

    @Schema(description = "优先级；数字越小越优先")
    private Integer priority;
}
