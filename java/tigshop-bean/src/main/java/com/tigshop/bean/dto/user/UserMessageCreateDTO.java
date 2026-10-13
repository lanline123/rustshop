// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.user.UserMessageConstants.TITLE_NOT_NULL;
import static com.tigshop.common.constant.user.UserMessageConstants.TITLE_OVER_LENGTH;

/**
 * 站内消息创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "站内消息参数")
public class UserMessageCreateDTO {
    @Schema(description = "消息标题")
    @NotBlank(message = TITLE_NOT_NULL)
    @Size(max = 50, message = TITLE_OVER_LENGTH)
    private String title;

    @Schema(description = "消息ID")
    private Integer messageId;

    @Schema(description = "消息日志ID")
    private Integer messageLogId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户等级")
    private Integer userRank;

    @Schema(description = "是否已读，0-未读，1-已读")
    private Boolean isRead;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "相关链接")
    private String link;

    @Schema(description = "添加时间（时间戳）")
    private Integer addTime;
}
