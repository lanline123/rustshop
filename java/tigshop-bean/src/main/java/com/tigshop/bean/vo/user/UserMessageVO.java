// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.user;

import cn.hutool.json.JSONObject;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 站内消息VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "站内消息参数")
public class UserMessageVO {
    @Schema(description = "站内消息ID")
    private Integer messageId;

    @Schema(description = "消息标题")
    @JsonTranslate
    private String title;

    @Schema(description = "消息日志ID")
    private Integer messageLogId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户等级")
    private Integer userRank;

    @Schema(description = "是否已读，0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "消息内容")
    @JsonTranslate
    private String content;

    @Schema(description = "相关链接")
    private JSONObject link;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "添加时间日期")
    private String addTimeDate;

    @Schema(description = "添加时间分秒")
    private String addTimeHms;

    @Schema(description = "订单信息")
    private OrderVO orderInfo;
}