// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.setting.wechat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 公众号关键字回复-规则下关键字子项 model，对应需求 §8.3
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Data
@TableName("wechat_keyword_rule_keyword")
@Schema(description = "公众号关键字回复-规则关键字子项")
public class WechatKeywordRuleKeyword implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属规则ID")
    @TableField("rule_id")
    private Long ruleId;

    @Schema(description = "关键字内容，≤30")
    private String keyword;

    @Schema(description = "匹配方式：1=全匹配 2=半匹配")
    @TableField("match_type")
    private Integer matchType;

    @Schema(description = "排序序号")
    @TableField("sort_order")
    private Integer sortOrder;
}
