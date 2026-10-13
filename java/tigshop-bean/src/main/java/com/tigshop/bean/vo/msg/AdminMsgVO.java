// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.msg;

import cn.hutool.json.JSONObject;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员消息VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "管理员消息参数")
public class AdminMsgVO {
    @Schema(description = "ID")
    private Integer msgId;

    @Schema(description = "消息类型")
    private Integer msgType;

    @Schema(description = "消息类型名称")
    private String msgTypeName;

    @Schema(description = "前端详情跳转标识，优先使用 relatedData.route")
    private String route;

    @Schema(description = "消息发送时间")
    private String sendTime;

    @Schema(description = "消息是否阅读, 1:已阅读; 0:未阅读")
    private Integer isReaded;

    @Schema(description = "消息是否发向指定店铺")
    private Integer shopId;

    @Schema(description = "消息是否发向指定管理员")
    private Integer adminId;

    @Schema(description = "订单消息")
    private Integer orderId;

    @Schema(description = "订单商品消息")
    private Integer productId;

    @Schema(description = "消息主题")
    @JsonTranslate(dataType = 1)
    private String title;

    @Schema(description = "消息内容")
    @JsonTranslate(dataType = 1)
    private String content;

    @Schema(description = "消息链接")
    private String msgLink;

    @Schema(description = "[json]跳转数据信息")
    private JSONObject relatedData;

}
