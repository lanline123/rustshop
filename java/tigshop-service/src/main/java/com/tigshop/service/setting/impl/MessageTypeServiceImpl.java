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

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.MessageTemplateDTO;
import com.tigshop.bean.dto.setting.MessageTypeDetailDTO;
import com.tigshop.bean.dto.setting.MessageTypeListDTO;
import com.tigshop.bean.enums.setting.MessageTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.MessageType;
import com.tigshop.bean.vo.setting.MessageTypeListVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.setting.MessageTypeMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.sms.SmsPlaceholderConverter;
import com.tigshop.service.setting.MessageTemplateService;
import com.tigshop.service.setting.MessageTypeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.tigshop.bean.enums.setting.MessageTypeEnum.fromCode;
import static com.tigshop.common.constant.Constants.DATE_FORMAT;
import static com.tigshop.common.constant.ResultTextConstants.FAIL_UPDATE;

/**
 * 消息类型
 *
 * @author Tigshop团队
 * @create 2024年12月25日 16:42
 */
@Slf4j
@Service
public class MessageTypeServiceImpl extends BaseServiceImpl<MessageTypeMapper, MessageType> implements MessageTypeService {
    @Resource
    private MessageTemplateService messageTemplateService;

    @Resource
    private ConfigService configService;

    @Resource
    private AdminLogService adminLogService;

    @Resource
    private WechatOAuthService wechatOAuthService;

    @Resource
    private TigshopProperties tigshopProperties;

    @Override
    public Page<MessageTypeListVO> list(MessageTypeListDTO dto) {
        Page<MessageType> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MessageType> queryWrapper = new LambdaQueryWrapper<>();
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        String keyword = dto.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(MessageType::getName, keyword);
        }
        Short sendType = dto.getSendType();
        if (sendType != null) {
            queryWrapper.eq(MessageType::getSendType, sendType);
        }
        List<MessageTypeListVO> list = this.list(page, queryWrapper).stream().filter(item -> {

            if (item.getName().startsWith("商家入驻")){
                //单商户不需要商家入驻
                if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
                    return false;
                }
            }
            return true;
        }).filter(item -> {
            if (item.getName().startsWith("会员实名认证通知")){
                //跨境、企业版、供应商需要实名认证
                return (tigshopProperties.getIsMerchant() == 1 && tigshopProperties.getIsB2b() == 1 );
            }
            return true;
        }).map(item -> {
            MessageTypeListVO vo = new MessageTypeListVO();
            BeanUtils.copyProperties(item, vo);
            //非跨境没有邮箱发送验证码
            if (tigshopProperties.getIsOverseas() != 1){
                if (item.getName().startsWith("获取验证码")){
                    vo.setIsMail(-1);
                }
            }
            //多商户|企业版本|供应商 入驻审核失败这三个通知（站内信、小程序、公众号）是不要的 || 跨境版本入驻审核失败的站内信也去掉
            if (tigshopProperties.getIsMerchant() == 1 && tigshopProperties.getIsOverseas() == 0){
                if (item.getName().startsWith("入驻审核失败")){
                    vo.setIsMessage(-1);
                    vo.setIsMiniProgram(-1);
                    vo.setIsWechat(-1);
                }
            }
            DateTime date = DateUtil.date(item.getAddTime() * 1000);
            vo.setAddTime(DateUtil.format(date, DATE_FORMAT));
            return vo;
        }).toList();
        return PageUtil.convertPage(page, list);
    }

    @Override
    public MessageTypeDetailDTO detail(Integer id) {
        MessageTypeDetailDTO messageTypeDetailDTO = new MessageTypeDetailDTO();
        // 获取消息设置详情
        MessageType messageType = this.getById(id);
        BeanUtils.copyProperties(messageType, messageTypeDetailDTO);

        String providerVal = configService.getConfigVal(SettingsEnum.SMS_PROVIDER);
        Integer providerCode = (providerVal != null && !providerVal.isEmpty()) ? Integer.parseInt(providerVal) : 1;
        messageTypeDetailDTO.setSmsProvider(providerCode);
        // 阿里云海外通道开关：供前端判定“海外短信模板”是否可编辑
        messageTypeDetailDTO.setSmsAliyunChannel(configService.getConfigVal(SettingsEnum.SMS_ALIYUN_CHANNEL));
        SmsProviderEnum smsProviderEnum = SmsProviderEnum.fromCode(providerCode);

        // 获取消息模板
        List<MessageTemplate> messageTemplates = messageTemplateService.listByMessageId(messageType.getMessageId());
        Map<String, MessageTemplateDTO> templateMessage = new HashMap<>();
        // 封装消息模板
        for (MessageTemplate messageTemplate : messageTemplates) {
            if (isSupportTemplate(messageType, messageTemplate.getType())) {
                continue;
            }
            MessageTypeEnum type = messageTemplate.getType();
            MessageTemplateDTO messageTemplateDTO = new MessageTemplateDTO();
            BeanUtils.copyProperties(messageTemplate, messageTemplateDTO);

            if (type == MessageTypeEnum.MESSAGE_TYPE_MSG || type == MessageTypeEnum.MESSAGE_TYPE_MESSAGE) {
                messageTemplateDTO.setContent(
                        SmsPlaceholderConverter.convert(messageTemplateDTO.getContent(), smsProviderEnum));
            }

            templateMessage.put(type.getKey(), messageTemplateDTO);
        }
        messageTypeDetailDTO.setTemplateMessage(templateMessage);
        return messageTypeDetailDTO;
    }

    private boolean isSupportTemplate(MessageType messageType, MessageTypeEnum type) {

        switch (type) {
            case MESSAGE_TYPE_WECHAT:
                return messageType.getIsWechat() == -1;
            case MESSAGE_TYPE_MIN_PROGRAM:
                return messageType.getIsMiniProgram() == -1;
            case MESSAGE_TYPE_MSG:
                return messageType.getIsMsg() == -1;
            case MESSAGE_TYPE_MESSAGE:
                return messageType.getIsMessage() == -1;
            case MESSAGE_TYPE_APP:
                return messageType.getIsApp() == -1;
            case MESSAGE_TYPE_DING:
                return messageType.getIsDing() == -1;
            case MESSAGE_TYPE_EMAIL:
                return messageType.getIsMail() == -1;
            case MESSAGE_TYPE_WXWORK:
                return messageType.getIsWxWork() == -1;
            default:
                return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(MessageTypeDetailDTO dto) {
        // 更新 MessageType 对象
        MessageType messageType = new MessageType();
        // 设置 MessageType 的值
        setMessageTypeFields(messageType, dto);

        boolean updated = this.updateById(messageType);
        if (updated) {
            // 遍历模板消息进行更新
            Map<String, MessageTemplateDTO> templateMessageMap = dto.getTemplateMessage();
            List<MessageTemplate> messageTemplateList = new ArrayList<>();
            templateMessageMap.forEach((key, templateData) -> {
                if (templateData != null) {
                    MessageTemplate messageTemplate = updateTemplate(key, templateData);
                    messageTemplateList.add(messageTemplate);
                }
            });
            if (!messageTemplateService.updateBatchById(messageTemplateList)) {
                throw new GlobalException(FAIL_UPDATE);
            }
        }
        adminLogService.createByLogInfo(StrUtil.format("更新消息类型:{}", getName(dto.getMessageId())));
        return updated;
    }

    @Override
    public void miniProgramMessageTemplate() {
        String accessToken = wechatOAuthService.getMiniAccessToken();
        // 删除已有模板
        deleteAllPrivateTemplatesMiniProgram(accessToken);
        // 添加模板
        addTemplate(accessToken, "31570", Arrays.asList(2, 1, 4), "订单支付成功通知");
        addTemplate(accessToken, "30766", Arrays.asList(4, 5, 3, 8, 2), "订单发货通知");
    }

    private void deleteAllPrivateTemplatesMiniProgram(String accessToken) {
        String listUrl = "https://api.weixin.qq.com/wxaapi/newtmpl/gettemplate?access_token=" + accessToken;

        String listResponse = HttpUtil.get(listUrl);
        JSONObject responseJson = JSONUtil.parseObj(listResponse);
        if (responseJson.containsKey("data") && responseJson.getInt("errcode") == 0) {
            JSONArray templateList = responseJson.getJSONArray("data");
            if (templateList != null) {
                String deleteUrl = "https://api.weixin.qq.com/wxaapi/newtmpl/deltemplate?access_token=" + accessToken;
                for (Object templateObj : templateList) {
                    JSONObject template = (JSONObject) templateObj;
                    JSONObject data = JSONUtil.createObj().set("priTmpId", template.getStr("priTmpId"));
                    String deleteResult = HttpUtil.post(deleteUrl, data.toString());
                    log.info("删除结果: {}", deleteResult);
                }
            }
        }
    }

    @Override
    public void wechatMessageTemplate() {
        String accessToken = wechatOAuthService.getMpAccessToken();
        // 删除已有模板
        deleteAllPrivateTemplates(accessToken);
        // 添加模板
        addTemplate(accessToken, "51617", Arrays.asList(3, 7, 4), "订单支付成功通知");
        addTemplate(accessToken, "48233", Arrays.asList(21, 18, 3, 17, 2), "订单发货通知");
        addTemplate(accessToken, "48058", Arrays.asList(5, 2), "退款成功通知");
    }

    private void deleteAllPrivateTemplates(String accessToken) {
        String listUrl = "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token=" + accessToken;

        String listResponse = HttpUtil.get(listUrl);
        JSONObject responseJson = JSONUtil.parseObj(listResponse);
        JSONArray templateList = responseJson.getJSONArray("template_list");

        if (templateList != null) {
            String deleteUrl = "https://api.weixin.qq.com/cgi-bin/template/del_private_template?access_token=" + accessToken;
            for (Object templateObj : templateList) {
                JSONObject template = (JSONObject) templateObj;
                JSONObject data = JSONUtil.createObj().set("template_id", template.getStr("template_id"));
                String deleteResult = HttpUtil.post(deleteUrl, data.toString());
                log.info("删除结果: {}", deleteResult);
            }
        }
    }

    private void addTemplate(String accessToken, String tid, List<Integer> kidList, String sceneDesc) {
        String url = "https://api.weixin.qq.com/wxaapi/newtmpl/addtemplate?access_token=" + accessToken;

        // 构建JSON请求体
        JSONObject payload = JSONUtil.createObj()
                .set("tid", tid)
                .set("kidList", kidList)
                .set("sceneDesc", sceneDesc);

        String result = HttpUtil.post(url, payload.toString());
        JSONObject resultJson = JSONUtil.parseObj(result);
        if (!resultJson.containsKey("priTmplId")) {
            throw new RuntimeException("添加模板失败: " + resultJson.getStr("errmsg"));
        }
    }

    /**
     * 设置 MessageType 的字段值
     */
    private void setMessageTypeFields(MessageType messageType, MessageTypeDetailDTO dto) {
        messageType.setMessageId(dto.getMessageId());
        Integer isMessage = dto.getIsMessage();
        if (isMessage != null && isMessage > -1) {
            messageType.setIsMessage(isMessage);
        }
        Integer isMsg = dto.getIsMsg();
        if (isMsg != null && isMsg > -1) {
            messageType.setIsMsg(isMsg);
        }
        Integer isWechat = dto.getIsWechat();
        if (isWechat != null && isWechat > -1) {
            messageType.setIsWechat(isWechat);
        }
        Integer isMiniProgram = dto.getIsMiniProgram();
        if (isMiniProgram != null && isMiniProgram > -1) {
            messageType.setIsMiniProgram(isMiniProgram);
        }
        Integer isApp = dto.getIsApp();
        if (isApp != null && isApp > -1) {
            messageType.setIsApp(isApp);
        }
        Integer isDing = dto.getIsDing();
        if (isDing != null && isDing > -1) {
            messageType.setIsDing(isDing);
        }
        Integer isMail = dto.getIsMail();
        if (isMail != null && isMail > -1) {
            messageType.setIsMail(isMail);
        }
        Integer isWxWork = dto.getIsWxWork();
        if (isWxWork != null && isWxWork > -1) {
            messageType.setIsWxWork(isWxWork);
        }
    }

    /**
     * 更新模板
     */
    private MessageTemplate updateTemplate(String key, MessageTemplateDTO templateData) {
        MessageTemplate messageTemplate = new MessageTemplate();
        BeanUtils.copyProperties(templateData, messageTemplate);

        MessageTypeEnum messageTypeEnum = fromCode(key);
        if (messageTypeEnum != null) {
            // 根据模板类型设置默认值
            switch (messageTypeEnum) {
                case MESSAGE_TYPE_MESSAGE, MESSAGE_TYPE_APP, MESSAGE_TYPE_EMAIL -> {
                    messageTemplate.setTemplateName(StringUtils.defaultIfEmpty(templateData.getTemplateName(), ""));
                    messageTemplate.setContent(StringUtils.defaultIfEmpty(templateData.getContent(), ""));
                }
                case MESSAGE_TYPE_MIN_PROGRAM, MESSAGE_TYPE_WECHAT ->
                        messageTemplate.setTemplateId(StringUtils.defaultIfEmpty(templateData.getTemplateId(), ""));
                case MESSAGE_TYPE_MSG -> {
                    messageTemplate.setTemplateId(StringUtils.defaultIfEmpty(templateData.getTemplateId(), ""));
                    // 海外短信明文文案：为 null 时不覆盖（非跨境版不下发该字段，避免清空已配置值）；有值（含空串）时写入
                    if (templateData.getContentOversea() != null) {
                        messageTemplate.setContentOversea(templateData.getContentOversea());
                    }
                }
                case MESSAGE_TYPE_DING, MESSAGE_TYPE_WXWORK ->
                        messageTemplate.setContent(StringUtils.defaultIfEmpty(templateData.getContent(), ""));
                default -> {
                }
            }
        }
        return messageTemplate;
    }

    /**
     * 获取消息名称
     */
    public String getName(Integer messageId) {
        LambdaQueryWrapper<MessageType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageType::getMessageId, messageId);
        return this.getOne(queryWrapper).getName();
    }
}