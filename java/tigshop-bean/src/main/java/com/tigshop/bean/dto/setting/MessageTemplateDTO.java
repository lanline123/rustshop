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

/**
 * 模板消息
 *
 * @author Tigshop团队
 * @create 2024年12月26日 10:27
 */
@Data
@Schema(description = "模板消息")
public class MessageTemplateDTO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "消息类型ID")
    private Integer messageId;

    @Schema(description = "消息类型 1模板消息 2订阅消息 3短信 4站内信 5app 6钉钉")
    private Integer type;

    @Schema(description = "模板类型名称")
    private String typeName;

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

    @Schema(description = "海外短信明文文案（国际/港澳台，仅阿里云海外通道使用）")
    private String contentOversea;
}
