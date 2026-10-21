package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.tigshop.bean.enums.finance.RuleScopeTypeEnum.SPECIFIED;

@Data
@TableName("category_service_fee_rule")
public class CategoryServiceFeeRule {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("rule_name")
    private String ruleName;

    @TableField("target_type")
    private Integer targetType;

    @TableField("scope_type")
    private Integer scopeType;

    @TableField("service_fee_rate")
    private BigDecimal serviceFeeRate;

    private String remark;

    private Integer status;

    private Integer deleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    public int getPrecisionLayer() {
        if (scopeType != null && scopeType == SPECIFIED.getCode()) {
            return 1;
        }
        return 2;
    }
}
