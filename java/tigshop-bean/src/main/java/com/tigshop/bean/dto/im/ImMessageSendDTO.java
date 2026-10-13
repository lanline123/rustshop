package com.tigshop.bean.dto.im;

import com.tigshop.bean.vo.im.ContentVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送消息
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */

@Data
@Schema(description = "发送消息参数")
public class ImMessageSendDTO {
    @Schema(description = "会话id")
    private Integer conversationId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "客服内容")
    private ContentVO content;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "消息类型：1用户发的，2客服发的")
    private Integer type;

    @Schema(description = "客服id")
    private Integer servantId;

    @Schema(description = "消息来源")
    private String userFrom;
}
