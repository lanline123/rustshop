package com.tigshop.bean.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话消息列表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会话消息参数")
public class ImMessageListVO {
    @Schema(description = "消息类型文本")
    private String messageTypeText;

    @Schema(description = "消息ID")
    private Integer id;

    @Schema(description = "会话ID")
    private Integer conversationId;

    @Schema(description = "内容")
    private ContentVO content;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "客服ID")
    private Integer servantId;

    @Schema(description = "发送时间")
    private String sendTime;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "扩展信息")
    private Object extend;

    @Schema(description = "推送状态")
    private Integer pushStatus;

    @Schema(description = "是否已读")
    private Integer isRead;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "客服信息")
    private ServantVO servant;

    @Data
    @Schema(description = "客服信息")
    public static class ServantVO {
        @Schema(description = "admin_id")
        private Integer adminId;

        @Schema(description = "客服名称")
        private String username;

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
