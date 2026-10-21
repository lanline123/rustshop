// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.im;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话消息VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会话消息参数")
public class ImMessageVO {
    @Schema(description = "消息类型文本")
    @JsonTranslate(dataType = 2)
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
}