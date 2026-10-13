package com.tigshop.bean.model.im;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话消息model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("im_message")
@Schema(description = "会话消息")
public class ImMessage implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "会话消息ID")
    private Integer id;

    @Schema(description = "来自会话")
    private Integer conversationId;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息类型：text文本 image图片 custom自定义 sound语音 video视频 file文件")
    private String messageType;

    @Schema(description = "类型：1用户发的，2客服发的，3系统(暂时没有)")
    private Integer type;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "客服ID")
    private Integer servantId;

    @Schema(description = "发送时间（时间戳）")
    private Long sendTime;

    @Schema(description = "状态：1正常，2删除，3撤回")
    private Integer status;

    @Schema(description = "消息扩展信息")
    private String extend;

    @Schema(description = "推送状态：0未推送，1已推送")
    private Integer pushStatus;

    @Schema(description = "是否已读：0未读，1已读")
    private Integer isRead;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "来源")
    private String userFrom;
}
