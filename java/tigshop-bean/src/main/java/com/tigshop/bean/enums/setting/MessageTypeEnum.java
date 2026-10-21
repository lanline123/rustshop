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

package com.tigshop.bean.enums.setting;

import com.baomidou.mybatisplus.annotation.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author Tigshop团队
 * @create  2024/12/26
 */
@Schema(description = "消息类型枚举")
@Getter
public enum MessageTypeEnum implements IEnum<Integer> {

    MESSAGE_TYPE_WECHAT(1, "wechatData", "微信模板消息"),
    MESSAGE_TYPE_MIN_PROGRAM(2, "miniProgramData", "小程序订阅消息"),
    MESSAGE_TYPE_MSG(3, "msgData", "短信"),
    MESSAGE_TYPE_MESSAGE(4, "messageData", "站内信"),
    MESSAGE_TYPE_APP(5, "appData", "APP"),
    MESSAGE_TYPE_DING(6, "dingData", "钉钉"),
    MESSAGE_TYPE_EMAIL(7, "emailData", "邮件"),
    MESSAGE_TYPE_WXWORK(8, "wxWorkData", "企业微信");

    private final int code;
    private final String key;
    private final String description;

    MessageTypeEnum(int code, String key, String description) {
        this.code = code;
        this.key = key;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return code;
    }

    public static MessageTypeEnum fromCode(String key) {
        for (MessageTypeEnum type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }
}