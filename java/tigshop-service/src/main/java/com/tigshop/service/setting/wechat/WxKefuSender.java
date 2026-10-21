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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 微信公众号「客服消息」异步发送器。
 * <p>
 * 配合「回复全部」模式（{@code reply_mode=2}）：被动回复 XML 只能返回 1 条消息，
 * 第 2..N 条由本组件通过客服消息接口 {@code message/custom/send} 主动下行补发。
 *
 * <p>实现要点：
 * <ul>
 *   <li>内部维护一个 <b>单线程</b> {@link ExecutorService}，保证「同一用户消息触发的多条补发」
 *       严格按原 sortOrder 顺序发送；并发不同用户之间不保证顺序</li>
 *   <li>{@link WxKefuCapabilityProbe#isEnabled()} 为 {@code false} 时整批跳过，仅记 info 日志</li>
 *   <li>单条发送失败只记 warn 日志（errcode / errmsg），不重试、不抛出、不影响后续条</li>
 *   <li>发送完成后以 info 日志输出汇总：规则 id / openid / 总数 / 成功数 / 失败数 / 首条失败原因</li>
 *   <li>线程命名 {@code wx-kefu-sender}，daemon 线程；Spring 销毁时优雅 shutdown</li>
 * </ul>
 *
 * <p>变量替换与 {@link WxMpKefuMessage} 构造统一委托给 {@link WxReplyRenderer#renderForKefu}，
 * 本组件只负责调度与容错，不重复处理渲染逻辑。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WxKefuSender {

    private final WxMpServiceHolder wxMpServiceHolder;

    private final WxKefuCapabilityProbe kefuCapabilityProbe;

    private final WxReplyRenderer wxReplyRenderer;

    /** 单线程串行执行器，保证同一批 followups 的发送顺序 */
    private ExecutorService executor;

    @PostConstruct
    void init() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wx-kefu-sender");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * 异步补发「回复全部」的第 2..N 条消息。
     * <p>
     * 单线程串行，保证同一用户消息内的发送顺序；
     * 单条失败只记 warn 日志，不重试、不抛出。
     *
     * @param openId    接收用户 openid
     * @param followups 第 2..N 条 payload（调用方须按 sortOrder 升序传入）
     * @param ruleId    触发规则 id（用于日志追踪）
     * @param wxMessage 原始入站消息（用于变量替换的上下文：fromUser 等）
     */
    public void sendAsync(String openId,
                          List<ReplyPayloadVO> followups,
                          Long ruleId,
                          WxMpXmlMessage wxMessage) {
        if (followups == null || followups.isEmpty()) {
            return;
        }
        executor.submit(() -> doSend(openId, followups, ruleId, wxMessage));
    }

    /**
     * 串行化发送实现：命中 {@link WxKefuCapabilityProbe#isEnabled()} 检查后逐条发送，
     * 统计成功 / 失败计数和首条失败原因，最后汇总输出一条 info 日志。
     */
    private void doSend(String openId,
                        List<ReplyPayloadVO> followups,
                        Long ruleId,
                        WxMpXmlMessage wxMessage) {
        if (!kefuCapabilityProbe.isEnabled()) {
            log.info("客服消息能力未开通，跳过补发，ruleId={} openid={} count={}",
                    ruleId, openId, followups.size());
            return;
        }
        WxMpService wxMpService = wxMpServiceHolder.get();
        if (wxMpService == null) {
            log.info("公众号未配置，跳过客服消息补发，ruleId={} openid={} count={}",
                    ruleId, openId, followups.size());
            return;
        }
        int successCount = 0;
        int failCount = 0;
        String firstError = null;
        for (int i = 0; i < followups.size(); i++) {
            ReplyPayloadVO payload = followups.get(i);
            try {
                WxMpKefuMessage msg = buildKefuMessage(openId, payload, wxMessage);
                if (msg == null) {
                    failCount++;
                    if (firstError == null) {
                        firstError = "payload 无效（index=" + i + "）";
                    }
                    log.warn("客服消息补发跳过无效 payload ruleId={} openid={} index={}",
                            ruleId, openId, i);
                    continue;
                }
                boolean ok = wxMpService.getKefuService().sendKefuMessage(msg);
                if (ok) {
                    successCount++;
                } else {
                    failCount++;
                    if (firstError == null) {
                        firstError = "sendKefuMessage 返回 false（index=" + i + "）";
                    }
                    log.warn("客服消息补发返回 false ruleId={} openid={} index={}",
                            ruleId, openId, i);
                }
            } catch (Exception e) {
                failCount++;
                String errmsg = e.getMessage();
                if (firstError == null) {
                    firstError = errmsg;
                }
                log.warn("客服消息补发失败 ruleId={} openid={} index={} err={}",
                        ruleId, openId, i, errmsg);
            }
        }
        log.info("客服消息补发汇总 ruleId={} openid={} total={} success={} fail={} firstError={}",
                ruleId, openId, followups.size(), successCount, failCount, firstError);
    }

    /**
     * 构造单条客服消息 {@link WxMpKefuMessage}。
     * <p>
     * 变量替换 / 文本或图文分支 / 图文字段完整性校验统一由
     * {@link WxReplyRenderer#renderForKefu(ReplyPayloadVO, String, WxMpXmlMessage)} 负责，
     * 本方法仅做空值守卫与异常兜底（失败返回 {@code null}，由外层统计为失败）。
     *
     * <p>注意：{@code renderForKefu} 方法由阶段 2 的 {@code WxReplyRenderer} 并行子代理产出，
     * 本文件编码时该方法可能暂不存在，属于预期的中间编译态，Round 1 结束后自动消除。
     */
    private WxMpKefuMessage buildKefuMessage(String openId,
                                             ReplyPayloadVO payload,
                                             WxMpXmlMessage wxMessage) {
        if (payload == null || payload.getMsgType() == null) {
            return null;
        }
        try {
            return wxReplyRenderer.renderForKefu(payload, openId, wxMessage);
        } catch (Exception e) {
            log.warn("构造客服消息失败 openid={} msgType={} err={}",
                    openId, payload.getMsgType(), e.getMessage());
            return null;
        }
    }
}
