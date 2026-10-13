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
 * 公众号关键字回复-规则下回复内容子项 model，对应需求 §8.4
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Data
@TableName("wechat_keyword_rule_reply")
@Schema(description = "公众号关键字回复-规则回复内容子项")
public class WechatKeywordRuleReply implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属规则ID")
    @TableField("rule_id")
    private Long ruleId;

    @Schema(description = "排序序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "消息类型：1=文本 2=图文")
    @TableField("msg_type")
    private Integer msgType;

    @Schema(description = "文本内容（msg_type=1 时必填）")
    @TableField("content_text")
    private String contentText;

    @Schema(description = "图文标题（msg_type=2），≤64")
    @TableField("news_title")
    private String newsTitle;

    @Schema(description = "图文封面图 URL（HTTPS 公网可访问），≤500")
    @TableField("news_cover_url")
    private String newsCoverUrl;

    @Schema(description = "图文摘要，≤120")
    @TableField("news_description")
    private String newsDescription;

    @Schema(description = "图文跳转链接，≤500")
    @TableField("news_url")
    private String newsUrl;
}
