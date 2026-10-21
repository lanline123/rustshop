package com.tigshop.bean.vo.user;

import com.tigshop.bean.model.user.UserMessageLog;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 站内信列表
 *
 * @author kidd
 * @since 2025/6/16 16:23
 */
@Data
public class UserMessageLogPageVO {

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

    @Schema(description = "是否撤回")
    private Integer isRecall;

    // *** Other ***

    @Schema(description = "发送时间")
    private String sendTime;

    public UserMessageLogPageVO(UserMessageLog userMessageLog) {
        this.messageLogId = userMessageLog.getMessageLogId();
        this.messageType = userMessageLog.getMessageType();
        this.sendUserType = userMessageLog.getSendUserType();
        this.userIds = userMessageLog.getUserIds();
        this.userRank = userMessageLog.getUserRank();
        this.messageTitle = userMessageLog.getMessageTitle();
        this.messageContent = userMessageLog.getMessageContent();
        this.messageLink = userMessageLog.getMessageLink();
        this.isRecall = userMessageLog.getIsRecall();

        this.sendTime = userMessageLog.getSendTime() == null ? null : TigUtils.handelTime(userMessageLog.getSendTime());
    }

}
