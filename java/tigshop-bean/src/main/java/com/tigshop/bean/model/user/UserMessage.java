// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 站内消息model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_message")
@Schema(description = "站内消息")
public class UserMessage implements Serializable {

    @TableId(value = "message_id", type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Integer messageId;

    @TableField("message_log_id")
    @Schema(description = "消息日志ID")
    private Integer messageLogId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Integer userId;

    @TableField("user_rank")
    @Schema(description = "用户等级")
    private Integer userRank;

    @TableField("is_read")
    @Schema(description = "是否已读，0-未读，1-已读")
    private Integer isRead;

    @TableField("title")
    @Schema(description = "消息标题")
    private String title;

    @TableField("content")
    @Schema(description = "消息内容")
    private String content;

    @TableField("message_template")
    @Schema(description = "消息内容")
    private String messageTemplate;

    @TableField("link")
    @Schema(description = "相关链接")
    private String link;

    @TableField("add_time")
    @Schema(description = "添加时间（时间戳）")
    private Long addTime;

    @TableField("type")
    @Schema(description = "消息类型")
    private Long type;
}