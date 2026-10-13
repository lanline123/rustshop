package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category_service_fee_rule_category")
public class CategoryServiceFeeRuleCategory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("rule_id")
    private Integer ruleId;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("category_name")
    private String categoryName;

    @TableField("category_path")
    private String categoryPath;

    private Integer deleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;
}
