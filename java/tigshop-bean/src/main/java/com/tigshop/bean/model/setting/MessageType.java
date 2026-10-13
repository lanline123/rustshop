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

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息类型
 *
 * @author Tigshop团队
 * @create 2024年12月25日 16:56
 */
@Data
@TableName("message_type")
@Schema(description = "消息类型")
public class MessageType implements Serializable {

    @Schema(description = "消息ID")
    @TableId(type = IdType.AUTO, value = "message_id")
    private Integer messageId;

    @Schema(description = "通知类型")
    private String name;

    @Schema(description = "通知描述")
    @TableField(value = "`describe`")
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

    @Schema(description = "是否支持邮件")
    private Integer isMail;

    @Schema(description = "是否支持企业微信")
    private Integer isWxWork;

    @Schema(description = "添加时间")
    private Long addTime;
}