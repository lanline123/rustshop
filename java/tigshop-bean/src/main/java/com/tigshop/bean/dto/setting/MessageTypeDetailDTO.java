// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 消息类型详情
 *
 * @author Tigshop团队
 * @create 2024年12月26日 10:30
 */
@Data
@Schema(description = "消息类型详情")
public class MessageTypeDetailDTO {
    @Schema(description = "消息ID")
    private Integer messageId;

    @Schema(description = "通知类型")
    private String name;

    @Schema(description = "通知描述")
    private String describe;

    @Schema(description = "发送类型 1会员 2商家")
    private Short sendType;

    @Schema(description = "是否支持微信公众号")
    private Integer isWechat;

    @Schema(description = "是否支持小程序")
    private Integer isMiniProgram;

    @Schema(description = "是否支持站内信")
    private Integer isMessage;

    @Schema(description = "是否支持短信")
    private Integer isMsg;

    @Schema(description = "是否支持app")
    private Integer isApp;

    @Schema(description = "是否支持钉钉")
    private Integer isDing;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "是否支持邮件")
    private Integer isMail;

    @Schema(description = "是否支持企业微信")
    private Integer isWxWork;

    @Schema(description = "当前短信服务商；1-阿里云，2-腾讯云，3-华为云，4-火山引擎")
    private Integer smsProvider;

    @Schema(description = "阿里云短信通道开关（复选：1=+86国内、2=其他海外、1,2=两者）；用于前端判定海外模板是否可编辑")
    private String smsAliyunChannel;

    @Schema(description = "模板消息")
    private Map<String, MessageTemplateDTO> templateMessage;
}
