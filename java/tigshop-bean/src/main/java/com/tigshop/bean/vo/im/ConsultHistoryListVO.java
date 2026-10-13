package com.tigshop.bean.vo.im;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客服历史会话列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "客服历史会话列表")
public class ConsultHistoryListVO {

    // *** ImConversation ***

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "最后客服ID")
    private Integer lastServantId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "会话状态：0待接入；1会话中；2已结束")
    private Integer status;

    @Schema(description = "最后更新时间")
    private String lastUpdateTime;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "会话备注")
    private String remark;

    @Schema(description = "会话总结")
    private String summary;

    // *** Other ***

    @Schema(description = "first_response_time")
    private Integer firstResponseTime;

    @Schema(description = "conversation_duration")
    private BigDecimal conversationDuration;











    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "average_response_time")
    private Integer averageResponseTime;

    @Schema(description = "servant")
    private ServantVO servant;

    @Schema(description = "first_user_message")
    private List<FirstUserMessageVO> firstUserMessage;

    @Schema(description = "客服信息")
    private List<ServantMessageVO> servantMessage;

    @Data
    @Schema(description = "客服消息")
    public static class ServantMessageVO {

        @Schema(description = "消息类型")
        private String messageTypeText;

        @Schema(description = "conversation_id")
        private Integer conversationId;

        @Schema(description = "servant_id")
        private Integer servantId;

        @Schema(description = "发送消息时间")
        private Long lastSendTime;

        @Schema(description = "first_send_time")
        private Long firstSendTime;

        @Schema(description = "消息数量")
        private Long messageCount;

        @Schema(description = "消息类型")
        private String messageType;

    }

    @Data
    @Schema(description = "首次用户消息")
    public static class FirstUserMessageVO {

        @Schema(description = "消息类型")
        private String messageTypeText;

        @Schema(description = "conversation_id")
        private Integer conversationId;

        @Schema(description = "用户Id")
        private Integer userId;

        @Schema(description = "发送消息时间")
        private String sendTime;

        @Schema(description = "message_type")
        private String messageType;
    }

    @Data
    @Schema(description = "客服信息")
    public static class ServantVO {
        @Schema(description = "admin_id")
        private Integer adminId;

        @Schema(description = "客服名称")
        private String username;

        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "mobile")
        private String mobile;

        @Schema(description = "avatar")
        private String avatar;
    }

    @Data
    @Schema(description = "用户信息")
    public static class UserVO {
        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;
    }
}
