package com.tigshop.bean.dto.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺保证金规则创建/更新参数。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金规则参数")
public class DepositRuleDTO {

    @Schema(description = "规则ID（更新时必填）")
    private Integer shopDepositRuleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "适用主体类型 1个人 2企业(MerchantType)，多选")
    private List<Integer> merchantTypes;

    @Schema(description = "适用店铺类型 1店铺 2供应商(DepositRuleShopType)，多选")
    private List<Integer> shopTypes;

    @Schema(description = "保证金金额")
    private BigDecimal depositAmount;

    @Schema(description = "生效开始时间（秒级时间戳）")
    private Long startTime;

    @Schema(description = "生效结束时间（秒级时间戳）")
    private Long endTime;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;

    @Schema(description = "适用类目ID列表（写入 shop_deposit_rule_category）")
    private List<Integer> categoryIds;
}
