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

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 会话消息列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "会话消息列表参数")
public class ImMessageListDTO extends BasePage {
    @Schema(description = "会话id")
    private Integer conversationId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "firstId")
    private Integer firstId;

    @Schema(description = "客服id")
    private Integer servantId;

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "是否可读")
    private Integer isRead;

}
