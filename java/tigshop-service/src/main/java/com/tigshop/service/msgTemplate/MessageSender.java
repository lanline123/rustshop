package com.tigshop.service.msgTemplate;

import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.enums.msg.MessageTemplateType;

/**
 * 消息发送器（模板消息）
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
public interface MessageSender {
    /**
     * 支持的类型
     * @param type
     * @return
     */
    boolean supports(MessageTemplateType type);

    /**
     * 发送消息到具体渠道
     * @param message
     */
    void send(Message message);
}
