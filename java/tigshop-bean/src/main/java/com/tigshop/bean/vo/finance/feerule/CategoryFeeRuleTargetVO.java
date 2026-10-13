package com.tigshop.bean.vo.finance.feerule;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CategoryFeeRuleTargetVO {

    private Integer ruleId;
    private String ruleName;
    private Integer precisionLayer;
    private BigDecimal serviceFeeRate;
    private List<String> categoryNames;
}
