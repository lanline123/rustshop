// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息模板参数
 *
 * @author Tigshop团队
 * @create 2025年03月04日 17:10
 */
@Data
@Schema(description = "消息模板参数")
public class MessageTemplateHandleDTO {
    @Schema(description = "站内信")
    private MessageTemplateChildDTO message;

    @Schema(description = "微信公众号")
    private MessageTemplateChildDTO wechat;

    @Schema(description = "小程序")
    private MessageTemplateChildDTO miniProgram;

    @Schema(description = "短信")
    private MessageTemplateChildDTO msg;

    @Schema(description = "app")
    private MessageTemplateChildDTO app;

    @Schema(description = "ding")
    private MessageTemplateChildDTO ding;

    @Schema(description = "email")
    private MessageTemplateChildDTO email;

    @Schema(description = "wxWork")
    private MessageTemplateChildDTO wxWork;

    @Schema(description = "消息类型")
    private MessageType typeInfo;

    @Data
    public static class MessageTemplateChildDTO {
        @Schema(description = "消息模板信息")
        private MessageTemplate info;
        @Schema(description = "是否禁用")
        private Integer disabled;

        public MessageTemplateChildDTO(MessageTemplate info, Integer disabled) {
            this.info = info;
            this.disabled = disabled;
        }
    }
}