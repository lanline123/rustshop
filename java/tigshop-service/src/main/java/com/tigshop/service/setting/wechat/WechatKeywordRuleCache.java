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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 关键字规则匹配缓存（进程内单例、30s TTL、全量加载 + Redis Pub/Sub 跨进程失效）。
 *
 * <p>设计取舍：
 * <ul>
 *   <li>规则总量上限 200（需求 §5.3），单进程容纳全量成本可忽略，
 *       无需 Caffeine —— 项目 pom.xml 也未显式引入 {@code com.github.benmanes.caffeine}，
 *       这里手写 30s TTL 容器避免新增依赖。</li>
 *   <li>后台写操作（save / del / setEnabled）完成后主动
 *       {@link #invalidateAndBroadcast()}，保证变更 &lt; 1s 内对 Dispatcher 生效；
 *       TTL 仅用于兜底，防止极端情况下 invalidate 未触发导致长期错匹配。</li>
 *   <li>{@link #snapshot(Supplier)} 采用 {@code synchronized} 串行化 loader 调用，
 *       避免多消息并发触发 loader 重入造成 DB 风暴；
 *       loader 抛异常时保留旧快照，首次加载失败则回退到 {@link Snapshot#empty()}。</li>
 * </ul>
 *
 * <p><b>多实例 / 多 JVM 一致性：</b>
 * 通过 Redis 频道 {@link #INVALIDATE_CHANNEL} 做 Pub/Sub 广播失效。
 * 典型部署是 {@code tigshop-adminapi}（后台写）+ {@code tigshop-api}（微信入站）
 * 两个独立 JVM，各自持有自己的快照；任一侧调用
 * {@link #invalidateAndBroadcast()} 会把消息发到该频道，订阅的所有实例
 * （含自身）都会执行本地 {@link #invalidate()}，保证跨进程秒级一致。
 * Redis 不可用时 {@link #invalidateAndBroadcast()} 退化为仅本地失效，
 * 对远端实例仍有 30s TTL 兜底，不会因 Redis 故障导致主流程失败。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Component
@Slf4j
public class WechatKeywordRuleCache implements MessageListener {

    /** Redis Pub/Sub 频道：用于跨进程广播缓存失效 */
    public static final String INVALIDATE_CHANNEL = "wechat:keyword-rule:invalidate";

    /**
     * 当前实例 ID，用于在广播里标注「发送方」。
     * 自己发出的消息虽然也会回到自己，但 {@link #invalidate()} 幂等，无需过滤。
     * 仅用于日志区分「远端触发」还是「本地触发」，便于排查。
     */
    private static final String INSTANCE_ID =
            ManagementFactory.getRuntimeMXBean().getName() + "#" + UUID.randomUUID();

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    /** 缓存存活时长：30 秒 */
    private static final long TTL_MS = 30_000L;

    /** 当前缓存的规则快照；null 表示尚未加载 */
    private volatile Snapshot snapshot;

    /** 快照过期时间戳（绝对毫秒值）；0 表示已失效 */
    private volatile long expireAt = 0L;

    /**
     * 获取快照。若不存在或已过期则通过 {@code loader} 重新加载；加载失败保留旧快照。
     *
     * @param loader 加载器，由 Service 层注入「扫描 + 拼装 RuleEntry」的具体实现
     * @return 当前有效快照（至少为 {@link Snapshot#empty()}）
     */
    public synchronized Snapshot snapshot(Supplier<Snapshot> loader) {
        long now = System.currentTimeMillis();
        if (snapshot != null && now < expireAt) {
            return snapshot;
        }
        try {
            Snapshot loaded = loader.get();
            snapshot = loaded == null ? Snapshot.empty() : loaded;
        } catch (Exception e) {
            log.warn("[WechatKeywordRuleCache] 加载规则快照失败，沿用旧快照或空快照", e);
            if (snapshot == null) {
                snapshot = Snapshot.empty();
            }
        }
        expireAt = now + TTL_MS;
        return snapshot;
    }

    /**
     * 主动失效「仅本地进程」的缓存。下一次 {@link #snapshot(Supplier)} 将强制触发 loader。
     * <p>Service 层调用入口请使用 {@link #invalidateAndBroadcast()}，以免漏掉
     * 跨 JVM 的远端实例；仅在「接收到 Redis 广播后回灌本地」场景下才直接调用本方法。
     */
    public synchronized void invalidate() {
        snapshot = null;
        expireAt = 0L;
    }

    /**
     * 主动失效「本地 + 所有订阅本频道的远端 JVM」的缓存。
     * <p>典型调用方：{@code WechatKeywordRuleServiceImpl} 在 save / del / setEnabled
     * 的事务 afterCommit 回调里调用。
     * <p>实现：先本地 {@link #invalidate()}，再向 Redis 频道
     * {@link #INVALIDATE_CHANNEL} 发一条短消息（payload 是发送方 instance id，仅用于日志追踪，
     * 不用于过滤——本地已在发送前清过）。Redis 不可用时退化为仅本地失效，
     * 主流程不抛异常，远端实例由 30s TTL 兜底。
     */
    public void invalidateAndBroadcast() {
        invalidate();
        StringRedisTemplate redis = this.stringRedisTemplate;
        if (redis == null) {
            log.warn("[KeywordRuleCache] StringRedisTemplate 未注入，跨进程广播跳过（仅本地失效）");
            return;
        }
        try {
            redis.convertAndSend(INVALIDATE_CHANNEL, INSTANCE_ID);
            log.info("[KeywordRuleCache] 已广播缓存失效 channel={} from={}",
                    INVALIDATE_CHANNEL, INSTANCE_ID);
        } catch (Exception e) {
            log.warn("[KeywordRuleCache] 广播缓存失效失败，已退化为仅本地失效（远端由 30s TTL 兜底）", e);
        }
    }

    // ====================== 订阅端：收到广播 → 清本地 ======================

    /**
     * 启动日志。订阅的动作由 {@code RedisConfig} 在
     * {@code RedisMessageListenerContainer} 中完成，这里只打印一下自身 ID 便于排查。
     */
    @PostConstruct
    private void logBootstrap() {
        log.info("[KeywordRuleCache] 初始化完成 instanceId={} channel={}",
                INSTANCE_ID, INVALIDATE_CHANNEL);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String from;
        try {
            from = message.getBody() == null
                    ? "unknown"
                    : new String(message.getBody(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            from = "decode-error";
        }
        boolean self = INSTANCE_ID.equals(from);
        invalidate();
        log.info("[KeywordRuleCache] 收到缓存失效广播 from={} self={}, 已清空本地快照",
                from, self);
    }

    // ====================== 快照数据结构 ======================

    /**
     * 规则快照：按 {@code created_at} 升序、id 升序稳定排列的启用中规则集合。
     */
    @Data
    @AllArgsConstructor
    public static class Snapshot {

        /** 启用中的规则条目，按 createdAt asc / id asc 排序 */
        private List<RuleEntry> rules;

        /** 空快照（首次加载失败或无启用规则时返回） */
        public static Snapshot empty() {
            return new Snapshot(Collections.emptyList());
        }
    }

    /**
     * 单条规则的匹配视图（含关键字 + 回复载荷），不含任何可变字段。
     */
    @Data
    @AllArgsConstructor
    public static class RuleEntry {

        /** 规则 ID */
        private Long ruleId;

        /** 规则名称（用于日志） */
        private String ruleName;

        /** 回复模式：1=随机一条 2=回复全部 */
        private Integer replyMode;

        /** 创建时间，用于同优先级下定序 */
        private LocalDateTime createdAt;

        /** 关键字列表（按 sort_order asc），参与匹配 */
        private List<KeywordEntry> keywords;

        /** 回复载荷（按 sort_order asc），供 Dispatcher 挑选 */
        private List<ReplyPayloadVO> replies;
    }

    /**
     * 单条关键字的匹配视图；保留原始大小写，匹配时由 Service 统一 lowerCase。
     */
    @Data
    @AllArgsConstructor
    public static class KeywordEntry {

        /** 关键字原文（保留大小写） */
        private String keyword;

        /** 匹配方式：1=全匹配 2=半匹配 */
        private Integer matchType;
    }
}
