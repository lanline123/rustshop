// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.msg;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.msg.AdminMsgConstants.ADMIN_MSG_ID_NOT_NULL;

/**
 * 管理员消息更新参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "管理员消息参数")
public class AdminMsgUpdateDTO {
    @Schema(description = "自增ID")
    @NotNull(message = ADMIN_MSG_ID_NOT_NULL)
    private Integer msgId;

    @Schema(description = "消息类型, 11:新订单, 12:已付款订单, 13:订单完成, 21:商品库存预警, 22:商品无货, 23:商品下架; 31:有效订单被取消, 32:售后申请, 41:店铺入驻申请, 42:店铺资质修改, 43:店铺违规")
    private Integer msgType;

    @Schema(description = "消息发送时间")
    private Integer sendTime;

    @Schema(description = "消息是否阅读, 1:已阅读; 0:未阅读")
    private Boolean isReaded;

    @Schema(description = "消息是否发向指定店铺")
    private Integer shopId;

    @Schema(description = "消息是否发向指定管理员")
    private Integer adminId;

    @Schema(description = "订单消息")
    private Integer orderId;

    @Schema(description = "订单商品消息")
    private Integer productId;

    @Schema(description = "消息主题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息链接")
    private String msgLink;

    @Schema(description = "[json]跳转数据信息")
    private String relatedData;
}