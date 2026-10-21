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

package com.tigshop.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.dto.setting.MessageTemplateContentDTO;
import com.tigshop.bean.dto.setting.MessageTemplateHandleDTO;
import com.tigshop.bean.enums.setting.MessageTemplateType;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.MessageType;
import com.tigshop.mapper.setting.MessageTemplateMapper;
import com.tigshop.mapper.setting.MessageTypeMapper;
import com.tigshop.service.setting.MessageTemplateService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tigshop.bean.enums.setting.MessageTypeEnum.*;

/**
 * 消息模板服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月26日 10:05
 */
@Service
public class MessageTemplateServiceImpl extends BaseServiceImpl<MessageTemplateMapper, MessageTemplate> implements MessageTemplateService {
    @Resource
    private MessageTypeMapper messageTypeMapper;

    @Override
    public List<MessageTemplate> listByMessageId(Integer messageId) {
        LambdaQueryWrapper<MessageTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageTemplate::getMessageId, messageId);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> getMiniProgramTemplateIds() {
        List<MessageTemplate> list = this.list(new LambdaUpdateWrapper<MessageTemplate>().eq(MessageTemplate::getType, 2).ne(MessageTemplate::getTemplateId, ""));
        //取出template_id组成list返回
        if (list != null) {
            return list.stream().map(MessageTemplate::getTemplateId).toList();
        }
        return List.of();
    }

    /**
     * 根据消息模板类型获取对应的模板id和内容
     * @param code 消息模板类型
     * @param content 消息内容
     * @return MessageTemplateContentDTO
     */
    public MessageTemplateContentDTO getMessageTemplate(String code, MessageTemplateContentDTO.TemplateContent content){
        MessageTemplateType mtt = MessageTemplateType.getByCode(code);
        if (mtt == null) {
            return null;
        }
        // 先通过消息模板的类型获取对应的消息模板
        MessageTemplateHandleDTO messageTemplateList = getMessageTemplateList(mtt.getMessageId());
        MessageType typeInfo = messageTemplateList.getTypeInfo();
        MessageTemplateHandleDTO.MessageTemplateChildDTO msg = messageTemplateList.getMsg();

        MessageTemplateContentDTO messageTemplateContentDTO = new MessageTemplateContentDTO();
        if (typeInfo != null && typeInfo.getIsMsg() == 1 && msg != null) {
            // 获取模板id，将对应的模板和内容封装到messageTemplateContentDTO中一同返回
            String templateId = msg.getInfo().getTemplateId();
            if (templateId != null) {
                messageTemplateContentDTO.setTemplateCode(templateId);
                messageTemplateContentDTO.setContent(content);
            }
        }
        return messageTemplateContentDTO;
    }

    /**
     * 处理对应的模板
     * @param messageId 消息类型id
     * @return MessageTemplateHandleDTO
     */
    public MessageTemplateHandleDTO getMessageTemplateList(Integer messageId){
        // 根据messageId查询消息类型
        MessageType messageType = messageTypeMapper.selectById(messageId);
        if (messageType == null) {
            return new MessageTemplateHandleDTO();
        }

        MessageTemplateHandleDTO messageTemplateHandleDTO = new MessageTemplateHandleDTO();
        messageTemplateHandleDTO.setTypeInfo(messageType);

        // 站内信处理
        MessageTemplate messageTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_MESSAGE.getCode());
        if (messageType.getIsMessage() == -1) {
            messageTemplateHandleDTO.setMessage(new MessageTemplateHandleDTO.MessageTemplateChildDTO(messageTemplate, 1));
        } else {
            messageTemplateHandleDTO.setMessage(new MessageTemplateHandleDTO.MessageTemplateChildDTO(messageTemplate, 0));
        }

        // 公众号处理
        MessageTemplate wechatTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_WECHAT.getCode());
        if (messageType.getIsWechat() == -1) {
            messageTemplateHandleDTO.setWechat(new MessageTemplateHandleDTO.MessageTemplateChildDTO(wechatTemplate, 1));
        } else {
            messageTemplateHandleDTO.setWechat(new MessageTemplateHandleDTO.MessageTemplateChildDTO(wechatTemplate, 0));

        }

        // 小程序处理
        MessageTemplate miniProgramTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_MIN_PROGRAM.getCode());
        if (messageType.getIsMiniProgram() == -1) {
            messageTemplateHandleDTO.setMiniProgram(new MessageTemplateHandleDTO.MessageTemplateChildDTO(miniProgramTemplate, 1));
        } else {
            messageTemplateHandleDTO.setMiniProgram(new MessageTemplateHandleDTO.MessageTemplateChildDTO(miniProgramTemplate, 0));
        }

        // 短信处理
        MessageTemplate msgTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_MSG.getCode());
        if (messageType.getIsMsg() == -1) {
            messageTemplateHandleDTO.setMsg(new MessageTemplateHandleDTO.MessageTemplateChildDTO(msgTemplate, 1));
        } else {
            messageTemplateHandleDTO.setMsg(new MessageTemplateHandleDTO.MessageTemplateChildDTO(msgTemplate, 0));
        }

        // APP处理
        MessageTemplate appTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_APP.getCode());
        if (messageType.getIsApp() == -1) {
            messageTemplateHandleDTO.setApp(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 1));
        } else {
            messageTemplateHandleDTO.setApp(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 0));
        }
        // 邮件处理
        MessageTemplate emailTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_EMAIL.getCode());
        if (messageType.getIsMail() == -1) {
            messageTemplateHandleDTO.setEmail(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 1));
        } else {
            messageTemplateHandleDTO.setEmail(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 0));
        }
        // 钉钉处理
        MessageTemplate dingTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_DING.getCode());
        if (messageType.getIsDing() == -1) {
            messageTemplateHandleDTO.setDing(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 1));
        } else {
            messageTemplateHandleDTO.setDing(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 0));
        }

        // 企业微信处理
        MessageTemplate wxWorkTemplate = buildMessageTemplate(messageId, MESSAGE_TYPE_WXWORK.getCode());
        if (messageType.getIsWxWork() == -1) {
            messageTemplateHandleDTO.setWxWork(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 1));
        } else {
            messageTemplateHandleDTO.setWxWork(new MessageTemplateHandleDTO.MessageTemplateChildDTO(appTemplate, 0));
        }

        return messageTemplateHandleDTO;
    }

    /**
     * 查询到对应的消息模板
     * @param messageId 消息类型id
     * @param messageType 消息类型
     * @return MessageTemplate
     */
    @Override
    public MessageTemplate buildMessageTemplate(Integer messageId, Integer messageType){
        return this.getOne(
                new LambdaQueryWrapper<MessageTemplate>()
                        .eq(MessageTemplate::getMessageId, messageId)
                        .eq(MessageTemplate::getType, messageType));
    }
}
