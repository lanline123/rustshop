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
 * 站内信
 */
@TableName(value = "user_message_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer messageLogId;

    @Schema(description = "消息类型")
    private Integer messageType;

    @Schema(description = "发送类型")
    private Integer sendUserType;

    @Schema(description = "用户集合")
    private String userIds;

    @Schema(description = "会员等级")
    private Integer userRank;

    @Schema(description = "标题")
    private String messageTitle;

    @Schema(description = "内容")
    private String messageContent;

    @Schema(description = "链接地址")
    private String messageLink;

    @Schema(description = "发送时间")
    private Long sendTime;

    @Schema(description = "是否撤回")
    private Integer isRecall;

    @TableField(exist = false)
    private String sendUserTypeText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}