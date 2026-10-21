package com.tigshop.service.msgTemplate.impl;

import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.mapper.user.UserMessageMapper;
import com.tigshop.service.msgTemplate.MessageSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 站内信发送
 */
@Slf4j
@Component
public class SiteSender implements MessageSender {

    @Resource
    UserMessageMapper userMessageMapper;

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.SITE;
    }

    @Override
    public void send(Message message) {
        try {
            UserMessage userMessage = new UserMessage();
            userMessage.setUserId((Integer)message.getMessage().get("user_id"));
            userMessage.setTitle((String) message.getMessage().get("title"));
            userMessage.setContent((String) message.getMessage().get("content"));
            userMessage.setLink(JSONUtil.toJsonStr(message.getMessage().get("link")));
            userMessage.setMessageTemplate((String) message.getMessage().get("messageTemplate"));
            userMessage.setAddTime(System.currentTimeMillis()/1000);
            userMessage.setType(message.getMessage().get("type") != null ? ((Number) message.getMessage().get("type")).longValue() : 0L);
            userMessageMapper.insert(userMessage);
            log.info("SiteSender 站内信发送成功: {}", JSONUtil.toJsonStr(userMessage));
        } catch (Exception e) {
            log.error("SiteSender 站内信发送失败: {}", e.getMessage());
        }
    }
}
