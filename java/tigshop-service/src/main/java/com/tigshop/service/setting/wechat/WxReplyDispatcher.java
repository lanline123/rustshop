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

import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 公众号关键字回复「命中规则」的发送编排器。
 * <p>输入命中规则（含 replies，已按 sortOrder 升序）+ 入站文本消息；
 * 输出「第 1 条被动回复」，并异步触发「第 2..N 条客服消息补发」。
 *
 * <p>reply_mode：
 * <ul>
 *   <li>1 随机回复一条：从 replies 随机选 1 条走被动回复，无 followups</li>
 *   <li>2 回复全部：第 1 条走被动回复，第 2..N 条交给 {@link WxKefuSender} 异步补发</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WxReplyDispatcher {

    private static final int REPLY_MODE_RANDOM = 1;
    private static final int REPLY_MODE_ALL    = 2;

    private final WxReplyRenderer wxReplyRenderer;
    private final WxKefuSender    wxKefuSender;

    /**
     * 调度命中规则的发送。
     *
     * @param hit       命中规则（含 replies）
     * @param wxMessage 原始入站消息
     * @return 用于 /api/user/login/wechatServer 同步 XML 回包的被动回复；
     *         若命中规则缺失 replies 则返回 null 让上游走默认回复
     */
    public WxMpXmlOutMessage dispatch(WechatKeywordRuleService.HitRule hit, WxMpXmlMessage wxMessage) {
        if (hit == null || hit.getReplies() == null || hit.getReplies().isEmpty()) {
            log.warn("命中规则无可用 replies，ruleId={} 走上游默认回复", hit == null ? null : hit.getRuleId());
            return null;
        }
        List<ReplyPayloadVO> replies = hit.getReplies();
        Integer mode = hit.getReplyMode() == null ? REPLY_MODE_RANDOM : hit.getReplyMode();

        ReplyPayloadVO first;
        List<ReplyPayloadVO> followups;
        if (mode == REPLY_MODE_ALL) {
            first = replies.get(0);
            followups = replies.size() > 1
                    ? new ArrayList<>(replies.subList(1, replies.size()))
                    : Collections.emptyList();
        } else {
            int idx = replies.size() == 1 ? 0 : ThreadLocalRandom.current().nextInt(replies.size());
            first = replies.get(idx);
            followups = Collections.emptyList();
        }

        WxMpXmlOutMessage passive = wxReplyRenderer.render(first, wxMessage);

        if (!followups.isEmpty()) {
            String openId = wxMessage.getFromUser();
            log.info("关键字回复全部模式，passive+{} 条 followup ruleId={} openid={}",
                    followups.size(), hit.getRuleId(), openId);
            wxKefuSender.sendAsync(openId, followups, hit.getRuleId(), wxMessage);
        }

        return passive;
    }
}
