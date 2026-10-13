package com.tigshop.bean.vo.finance.feerule;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单点匹配结果（缓存 / 内部）
 */
@Data
public class CategoryFeeRuleMatchVO {

    private Boolean matched;
    private Integer ruleId;
    private String ruleName;
    private Integer precisionLayer;
    private BigDecimal serviceFeeRate;
    private Integer scopeType;
    /** 命中层级对应 RateSourceEnum.value */
    private String rateSource;
}
