// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.im.ImConversationConstants.IM_CONVERSATION_ID_NOT_NULL;

/**
 * 客服会话更新参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "客服会话参数")
public class ImConversationUpdateDTO {
    @Schema(description = "客服会话ID")
    @NotNull(message = IM_CONVERSATION_ID_NOT_NULL)
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "最后客服ID")
    private Integer lastServantId;

    @Schema(description = "添加时间（时间戳）")
    private Integer addTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "会话状态：0待接入；1会话中；2已结束")
    private Integer status;

    @Schema(description = "最后更新时间（时间戳）")
    private Integer lastUpdateTime;

    @Schema(description = "是否删除：0未删除；1已删除")
    private Integer isDelete;

    @Schema(description = "会话备注")
    private String remark;

    @Schema(description = "会话总结")
    private String summary;
}
