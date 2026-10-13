// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.setting.wechat.WxMenuRuleDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 微信公众号回复渲染器。
 * <p>
 * 把 {@link ReplyPayloadVO}（自动回复 / 关键字回复通用载荷）渲染为 WxJava 的
 * {@link WxMpXmlOutMessage} —— 文本或图文。负责变量占位符替换：
 * <ul>
 *   <li>{@value #PH_NICKNAME} —— 用户昵称（取自 {@link WxUserInfoCache}）</li>
 *   <li>{@value #PH_SHOP_NAME} —— 商城简称（取自 {@link SettingsEnum#SHOP_NAME}）</li>
 *   <li>{@value #PH_DATE} —— 当前日期（{@code yyyy-MM-dd}）</li>
 * </ul>
 * 图文消息中仅替换 {@code newsTitle} / {@code newsDescription}，URL 字段不做替换。
 *
 * <p>容错策略：
 * <ul>
 *   <li>{@link #render(ReplyPayloadVO, WxMpXmlMessage)} 顶层 try/catch，任何异常都回落到
 *       「服务暂不可用」文本兜底</li>
 *   <li>图文必填字段（标题/封面/摘要/链接）任一为空则同样兜底</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WxReplyRenderer {

    /** 兜底文本 */
    private static final String FALLBACK_CONTENT = "服务暂不可用";

    private static final String PH_NICKNAME = "{nickname}";
    private static final String PH_SHOP_NAME = "{shopName}";
    private static final String PH_DATE = "{date}";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 文本消息 msgType 常量 */
    private static final int MSG_TYPE_TEXT = 1;
    /** 图文消息 msgType 常量 */
    private static final int MSG_TYPE_NEWS = 2;

    private final WxUserInfoCache wxUserInfoCache;

    private final ConfigService configService;

    /**
     * 渲染回复消息：根据 {@code payload.msgType} 输出文本或图文 XML 出参。
     * 顶层 try/catch 包住整个方法，任意异常都回落到 {@link #fallback(WxMpXmlMessage)}。
     */
    public WxMpXmlOutMessage render(ReplyPayloadVO payload, WxMpXmlMessage wxMessage) {
        try {
            if (payload == null || payload.getMsgType() == null) {
                return fallback(wxMessage);
            }
            String from = wxMessage.getToUser();
            String to = wxMessage.getFromUser();

            int msgType = payload.getMsgType();
            if (msgType == MSG_TYPE_TEXT) {
                String content = replaceVariables(payload.getContent(), wxMessage);
                return WxMpXmlOutMessage.TEXT()
                        .content(content == null ? "" : content)
                        .fromUser(from)
                        .toUser(to)
                        .build();
            }

            if (msgType == MSG_TYPE_NEWS) {
                if (StrUtil.hasBlank(payload.getNewsTitle(),
                        payload.getNewsDescription(),
                        payload.getNewsCoverUrl(),
                        payload.getNewsUrl())) {
                    log.warn("图文消息字段不完整，回退到兜底文本：title={}, desc={}, cover={}, url={}",
                            payload.getNewsTitle(),
                            payload.getNewsDescription(),
                            payload.getNewsCoverUrl(),
                            payload.getNewsUrl());
                    return fallback(wxMessage);
                }
                WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
                item.setTitle(replaceVariables(payload.getNewsTitle(), wxMessage));
                item.setDescription(replaceVariables(payload.getNewsDescription(), wxMessage));
                item.setPicUrl(payload.getNewsCoverUrl());
                item.setUrl(payload.getNewsUrl());
                return WxMpXmlOutMessage.NEWS()
                        .addArticle(item)
                        .fromUser(from)
                        .toUser(to)
                        .build();
            }

            log.warn("未知 msgType={}，回退到兜底文本", msgType);
            return fallback(wxMessage);
        } catch (Exception e) {
            log.warn("渲染微信回复消息失败，回退到兜底文本", e);
            return fallback(wxMessage);
        }
    }

    /**
     * 把 {@link ReplyPayloadVO} 渲染为 WxJava 客服消息 {@link WxMpKefuMessage}，
     * 供「回复全部」的第 2..N 条通过客服消息主动下行使用。
     *
     * <p>变量占位符规则与 {@link #render(ReplyPayloadVO, WxMpXmlMessage)} 一致。
     *
     * <p>图文消息的处理：由于 WxJava 4.7.0 的 {@code WxMpKefuMessage.NEWS()} 需要永久图文素材 media_id，
     * 不适合动态场景；本方法把 msgType=2 的图文降级为文本消息，格式为
     * {@code "【{newsTitle}】\n{newsDescription}\n{newsUrl}"}，变量仍会替换。
     *
     * @param payload   回复载荷（已按 sortOrder 挑选好的单条）
     * @param openId    接收用户 openid（作为客服消息 toUser）
     * @param wxMessage 原始入站消息（用于变量替换的上下文，尤其是 fromUser → nickname）
     * @return 渲染后的客服消息；若 payload 非法（空 / msgType 不识别 / 图文字段缺失）返回 null，调用方记 warn 并跳过
     */
    public WxMpKefuMessage renderForKefu(ReplyPayloadVO payload, String openId, WxMpXmlMessage wxMessage) {
        try {
            if (payload == null || payload.getMsgType() == null || StrUtil.isBlank(openId)) {
                return null;
            }
            int msgType = payload.getMsgType();
            if (msgType == MSG_TYPE_TEXT) {
                String content = replaceVariables(payload.getContent(), wxMessage);
                return WxMpKefuMessage.TEXT()
                        .toUser(openId)
                        .content(content == null ? "" : content)
                        .build();
            }
            if (msgType == MSG_TYPE_NEWS) {
                if (StrUtil.hasBlank(payload.getNewsTitle(), payload.getNewsDescription(),
                        payload.getNewsCoverUrl(), payload.getNewsUrl())) {
                    log.warn("客服消息图文字段不完整，跳过：title={} desc={} cover={} url={}",
                            payload.getNewsTitle(), payload.getNewsDescription(),
                            payload.getNewsCoverUrl(), payload.getNewsUrl());
                    return null;
                }
                String title = replaceVariables(payload.getNewsTitle(), wxMessage);
                String desc = replaceVariables(payload.getNewsDescription(), wxMessage);
                String url = payload.getNewsUrl();
                // 降级为文本（WxJava 4.7.0 限制：客服消息图文需要素材 media_id）
                String text = String.format("【%s】\n%s\n%s",
                        title == null ? "" : title,
                        desc == null ? "" : desc,
                        url == null ? "" : url);
                return WxMpKefuMessage.TEXT().toUser(openId).content(text).build();
            }
            log.warn("客服消息未知 msgType={}，跳过", msgType);
            return null;
        } catch (Exception e) {
            log.warn("客服消息渲染失败，跳过", e);
            return null;
        }
    }

    /**
     * 将菜单「发送消息」规则（click 叶子）转为 {@link ReplyPayloadVO}，供菜单 CLICK 回调渲染。
     */
    public static ReplyPayloadVO fromMenuClickRule(WxMenuRuleDTO rule) {
        ReplyPayloadVO vo = new ReplyPayloadVO();
        if (rule == null) {
            return vo;
        }
        vo.setMsgType(rule.getMsgType());
        vo.setContent(rule.getContent());
        vo.setNewsTitle(rule.getNewsTitle());
        vo.setNewsCoverUrl(rule.getNewsCoverUrl());
        vo.setNewsDescription(rule.getNewsDescription());
        vo.setNewsUrl(rule.getNewsUrl());
        return vo;
    }

    /**
     * 三占位符替换（按需懒计算昵称/商城名）。空文本直接原样返回。
     */
    private String replaceVariables(String raw, WxMpXmlMessage wxMessage) {
        if (StrUtil.isBlank(raw)) {
            return raw;
        }
        String result = raw;
        if (result.contains(PH_NICKNAME)) {
            String nickname = wxUserInfoCache.getNickname(wxMessage.getFromUser());
            result = result.replace(PH_NICKNAME, nickname == null ? "" : nickname);
        }
        if (result.contains(PH_SHOP_NAME)) {
            String shopName = configService.getConfigVal(SettingsEnum.SHOP_NAME);
            result = result.replace(PH_SHOP_NAME, shopName == null ? "" : shopName);
        }
        if (result.contains(PH_DATE)) {
            result = result.replace(PH_DATE, LocalDate.now().format(DATE_FMT));
        }
        return result;
    }

    /**
     * 兜底文本：用于异常 / 字段缺失场景，保证用户始终能收到一条 200 OK 的文本回复。
     */
    private WxMpXmlOutMessage fallback(WxMpXmlMessage wxMessage) {
        return WxMpXmlOutMessage.TEXT()
                .content(FALLBACK_CONTENT)
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .build();
    }
}
