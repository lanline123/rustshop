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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 管理员消息创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员消息参数")
public class AdminMsgCreateDTO {
    @Schema(description = "消息类型：11新订单、12已付款订单、13订单完成、21-26商品消息、31订单取消、32售后申请、33-37财务消息、41-43店铺服务、44保证金预警、51-57其它消息、54发货提醒；15/16为历史兼容类型，不展示分类")
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

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "[json]跳转数据信息")
    private Map<String, Object> relatedData;

    @Schema(description = "是否参与语音播报: 1-播报, 0-不播报；不传默认播报")
    private Integer isBroadcast;
}
