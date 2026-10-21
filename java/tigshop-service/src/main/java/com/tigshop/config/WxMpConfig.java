package com.tigshop.config;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.wechat.WechatAutoReplyService;
import com.tigshop.service.setting.wechat.WechatKeywordRuleService;
import com.tigshop.service.setting.wechat.WechatMenuService;
import com.tigshop.service.setting.wechat.WxReplyDispatcher;
import com.tigshop.service.setting.wechat.WxReplyRenderer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static me.chanjar.weixin.common.api.WxConsts.EventType.*;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;

/**
 * @author wzh
 */
@Slf4j
@Configuration
public class WxMpConfig {

    @Resource
    private ConfigService configService;

    @Resource
    private WechatAutoReplyService wechatAutoReplyService;

    @Resource
    private WechatMenuService wechatMenuService;

    @Resource
    private WxReplyRenderer wxReplyRenderer;

    @Resource
    private WechatKeywordRuleService wechatKeywordRuleService;

    /**
     * 打破循环依赖：WxMpConfig → WxReplyDispatcher → WxKefuSender → WxMpService（由本类产出）。
     * 用 @Lazy 延后到首次使用时解析，此刻 wxService bean 已完成注册。
     */
    @Resource
    @Lazy
    private WxReplyDispatcher wxReplyDispatcher;

    /**
     * 创建并配置一个用于微信公众号操作的 WxMpService 实例。
     * 该方法读取配置属性并为每个公众号配置创建一个 WxMpDefaultConfigImpl 实例，
     * 将这些实例放入 MultiConfigStorages 中，并返回最终的 WxMpService 实例。
     *
     * @return WxMpService 实例，用于微信公众号操作
     * @throws RuntimeException 如果 wxService 配置异常或 Application 相关配置对象缺失
     */
    @Bean
    public WxMpService wxService() {
        ConfigPO wechatAppId = configService.getConfigByCode(SettingsEnum.WECHAT_APP_ID.getBizCode());
        ConfigPO wechatAppSecret = configService.getConfigByCode(SettingsEnum.WECHAT_APP_SECRET.getBizCode());
        ConfigPO wechatServerToken = configService.getConfigByCode(SettingsEnum.WECHAT_SERVER_TOKEN.getBizCode());
        ConfigPO wechatServerSecret = configService.getConfigByCode(SettingsEnum.WECHAT_SERVER_SECRET.getBizCode());
        // 检查配置是否存在
        List<ConfigPO> configs = Arrays.asList(wechatAppId, wechatAppSecret, wechatServerToken, wechatServerSecret);
        if (configs.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("微信公众号配置缺失，请检查配置项");
        }
        // 创建 WxMpService 实例
        WxMpService service = new WxMpServiceImpl();
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(wechatAppId.getBizVal());
        config.setSecret(wechatAppSecret.getBizVal());
        config.setToken(wechatServerToken.getBizVal());
        config.setAesKey(wechatServerSecret.getBizVal());
        service.setWxMpConfigStorage(config);
        return service;
    }

    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(subscribeHandler()).end();

        // 取消关注事件
        newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(unsubscribeHandler()).end();

        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(SCAN).handler(scanHandler()).end();

        // 自定义菜单 click：Event=CLICK，EventKey 对应菜单「发送消息」落库配置（与关键词自动回复无关）
        newRouter.rule().async(false).msgType(EVENT).event(WxConsts.EventType.CLICK).handler(menuClickHandler()).end();

        // 文本消息
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.TEXT).handler(textMsgHandler()).end();

        // 默认
        newRouter.rule().async(false).handler(defaultHandler()).end();
        return newRouter;
    }

    // 处理用户扫码关注事件
    // 关注事件处理器
    @Bean
    public WxMpMessageHandler subscribeHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            String openId = wxMessage.getFromUser();
            try {
                String eventKey = wxMessage.getEventKey();
                String sceneId = null;
                if (eventKey != null && eventKey.startsWith("qrscene_")) {
                    sceneId = eventKey.substring(8);
                }
                log.info("用户关注，OpenID: {}，场景值: {}", openId, sceneId);
                // TODO: 处理关注逻辑（记录用户/分发 sceneId）

                return wxReplyRenderer.render(
                        wechatAutoReplyService.getSubscribe(),
                        wxMessage);
            } catch (Exception e) {
                log.warn("subscribeHandler 处理失败，OpenID: {}", openId, e);
                return wxReplyRenderer.render(null, wxMessage);
            }
        };
    }

    // 取消关注事件处理器
    @Bean
    public WxMpMessageHandler unsubscribeHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 获取用户OpenID
            String openId = wxMessage.getFromUser();

            // TODO: 处理取消关注逻辑，如标记用户状态
            System.out.println("用户取消关注，OpenID: " + openId);

            // 取消关注事件不需要回复消息，返回null
            return null;
        };
    }

    // 扫码事件处理器（已关注用户）
    @Bean
    public WxMpMessageHandler scanHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 获取二维码场景值
            String sceneId = wxMessage.getEventKey();

            // 获取用户OpenID
            String openId = wxMessage.getFromUser();

            // TODO: 处理扫码逻辑，如根据场景值执行不同操作
            System.out.println("用户扫码，OpenID: " + openId + "，场景值: " + sceneId);

            // 带 ticket 的扫码由 UserServiceImpl 作为 PC 登录事件处理，并通过客服消息发送登录成功配置。
            // 不再额外返回历史固定文本，避免覆盖/干扰“登录成功回复”配置；普通扫码仍保留原提示。
            if (wxMessage.getTicket() != null && !wxMessage.getTicket().isBlank()) {
                return null;
            }

            // 返回扫码响应消息
            return WxMpXmlOutMessage.TEXT()
                    .content("您扫描了场景: " + sceneId)
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser())
                    .build();
        };
    }

    /**
     * 菜单 click 事件：MsgType=event、Event=CLICK，EventKey 与自定义菜单发布时的 click.key 一致，
     * 按 Key 从当前菜单 JSON 取「发送消息」配置（文本/图文），不经过关键词规则匹配。
     */
    @Bean
    public WxMpMessageHandler menuClickHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            String openId = wxMessage.getFromUser();
            String rawKey = wxMessage.getEventKey();
            try {
                String eventKey = rawKey == null ? "" : rawKey.trim();
                log.info("收到菜单点击，OpenID: {}，EventKey: {}", openId, eventKey);

                if (eventKey.isEmpty()) {
                    return wxReplyRenderer.render(
                            wechatAutoReplyService.getDefault(), wxMessage);
                }

                ReplyPayloadVO payload = wechatMenuService.findMenuClickPayload(eventKey);
                if (payload != null && payload.getMsgType() != null) {
                    return wxReplyRenderer.render(payload, wxMessage);
                }

                return wxReplyRenderer.render(
                        wechatAutoReplyService.getDefault(), wxMessage);
            } catch (Exception e) {
                log.warn("menuClickHandler 处理失败，OpenID: {}, eventKey: {}", openId, rawKey, e);
                return wxReplyRenderer.render(null, wxMessage);
            }
        };
    }

    // 文本消息处理器
    @Bean
    public WxMpMessageHandler textMsgHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            String openId = wxMessage.getFromUser();
            String rawContent = wxMessage.getContent();
            try {
                String content = rawContent == null ? "" : rawContent.trim();
                log.info("收到文本消息，OpenID: {}，内容: {}", openId, content);

                if (content.isEmpty()) {
                    return wxReplyRenderer.render(
                            wechatAutoReplyService.getDefault(), wxMessage);
                }

                WechatKeywordRuleService.HitRule hit =
                        wechatKeywordRuleService.findFirstHitRule(content);
                if (hit != null) {
                    WxMpXmlOutMessage passive = wxReplyDispatcher.dispatch(hit, wxMessage);
                    if (passive != null) {
                        return passive;
                    }
                    // dispatcher 内部判定命中规则无可用 replies，走默认回复兜底
                }

                return wxReplyRenderer.render(
                        wechatAutoReplyService.getDefault(), wxMessage);
            } catch (Exception e) {
                log.warn("textMsgHandler 处理失败，OpenID: {}, content: {}", openId, rawContent, e);
                return wxReplyRenderer.render(null, wxMessage);
            }
        };
    }

    // 默认处理器
    @Bean
    public WxMpMessageHandler defaultHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            String openId = wxMessage.getFromUser();
            try {
                log.info("收到非文本消息，OpenID: {}, MsgType: {}",
                        openId, wxMessage.getMsgType());
                return wxReplyRenderer.render(
                        wechatAutoReplyService.getDefault(), wxMessage);
            } catch (Exception e) {
                log.warn("defaultHandler 处理失败，OpenID: {}", openId, e);
                return wxReplyRenderer.render(null, wxMessage);
            }
        };
    }
}
