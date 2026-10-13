package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺保证金规则VO（列表 + 详情回显）。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金规则")
public class DepositRuleVO {

    @Schema(description = "规则ID")
    private Integer shopDepositRuleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "适用主体类型 1个人 2企业，多选")
    private List<Integer> merchantTypes;

    @Schema(description = "适用主体类型文本")
    private String merchantTypeText;

    @Schema(description = "适用店铺类型 1店铺 2供应商，多选")
    private List<Integer> shopTypes;

    @Schema(description = "适用店铺类型文本")
    private String shopTypeText;

    @Schema(description = "保证金金额")
    private BigDecimal depositAmount;

    @Schema(description = "生效开始时间（秒级时间戳）")
    private Long startTime;

    @Schema(description = "生效结束时间（秒级时间戳）")
    private Long endTime;

    @Schema(description = "生效开始时间文本")
    private String startTimeText;

    @Schema(description = "生效结束时间文本")
    private String endTimeText;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "适用类目ID列表")
    private List<Integer> categoryIds;

    @Schema(description = "适用类目名称列表")
    private List<String> categoryNames;

    @Schema(description = "创建时间文本")
    private String addTime;
}
