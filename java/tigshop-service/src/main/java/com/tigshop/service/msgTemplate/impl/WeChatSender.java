package com.tigshop.service.msgTemplate.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.oauth.WechatOAuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微信公众号发送
 * @author Admin
 */
@Slf4j
@Component
public class WeChatSender implements MessageSender {

    @Resource
    private WechatOAuthService wechatOAuthService;

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.Wechat;
    }

    @Override
    public void send(Message message) {
        try {
            String accessToken = wechatOAuthService.getMpAccessToken();
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
            JSONObject data = JSONUtil.parseObj(message.getMessage());
            String result = HttpUtil.post(url, data.toString());
            log.info("WeChatSender 发送结果: {}", result);
        } catch (Exception e) {
            log.error("WeChatSender 发送失败", e);
        }
    }
}
