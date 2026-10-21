package com.tigshop.bean.vo.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "费率规则列表项")
public class CategoryFeeRuleListVO {

    private Integer id;
    private String ruleName;
    private Integer targetType;
    private String targetTypeName;
    private Integer scopeType;
    private String scopeTypeName;
    private Integer precisionLayer;
    private BigDecimal serviceFeeRate;
    private Integer targetCount;
    private Integer categoryCount;
    private Integer status;
    private LocalDateTime createTime;
}
