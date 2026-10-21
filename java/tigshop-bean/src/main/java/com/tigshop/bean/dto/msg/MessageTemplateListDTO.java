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


import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.MessageType;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/5/26 10:59
 */
@Data
public class MessageTemplateListDTO {

    private MessageTemplateListVO message;
    private MessageTemplateListVO wechat;
    private MessageTemplateListVO miniProgram;
    private MessageTemplateListVO msg;
    private MessageTemplateListVO app;
    private MessageTemplateListVO mail;
    private MessageTemplateListVO wxWork;
    private MessageTemplateListVO ding;

    private MessageType typeInfo;

    @Data
    public static class MessageTemplateListVO {
        private Integer disabled;
        private MessageTemplate info;
    }
}
