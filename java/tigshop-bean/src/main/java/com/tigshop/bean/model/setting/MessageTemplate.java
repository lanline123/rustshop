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
import com.tigshop.bean.enums.setting.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板消息表
 *
 * @author Tigshop团队
 * @create 2024-12-26
 */
@Data
@TableName("message_template")
@Schema(description = "模板消息表")
public class MessageTemplate {

    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "消息类型ID")
    private Integer messageId;

    @Schema(description = "消息类型 1模板消息 2订阅消息 3短信 4站内信 5app 6钉钉 7邮件 8企业微信")
    @TableField("type")
    private MessageTypeEnum type;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "发送对象(暂只用于钉钉)")
    private String toUserid;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板编号")
    private String templateNum;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "海外短信明文文案（国际/港澳台，仅阿里云海外通道 SendMessageToGlobe 使用）")
    @TableField("content_oversea")
    private String contentOversea;
}