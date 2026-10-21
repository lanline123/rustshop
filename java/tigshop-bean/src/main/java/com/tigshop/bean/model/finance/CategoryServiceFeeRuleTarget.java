package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category_service_fee_rule_target")
public class CategoryServiceFeeRuleTarget {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("rule_id")
    private Integer ruleId;

    @TableField("target_id")
    private Integer targetId;

    private Integer deleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;
}
