package com.tigshop.service.product;

import cn.hutool.core.lang.tree.Tree;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 商品分类可见树快照（进程内、按 locale 分桶、5 分钟 TTL 兜底 + Redis Pub/Sub 跨进程失效）。
 *
 * <p>设计参照 {@link com.tigshop.service.setting.wechat.WechatKeywordRuleCache}：
 * synchronized 单飞 loader、失效后秒级重建、对外返回防御性拷贝避免污染共享快照。
 */
@Component
@Slf4j
public class CategoryTreeCache implements MessageListener {

    public static final String INVALIDATE_CHANNEL = "category:tree:invalidate";

    private static final String INSTANCE_ID =
            ManagementFactory.getRuntimeMXBean().getName() + "#" + UUID.randomUUID();

    /** 快照 TTL 兜底：5 分钟 */
    private static final long TTL_MS = 5 * 60 * 1000L;

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    private final Map<String, LocaleBucket> buckets = new HashMap<>();

    /**
     * 获取指定 locale 的快照；过期或不存在时通过 loader 重建。
     */
    public synchronized Snapshot snapshot(Supplier<Snapshot> loader, String localeKey) {
        String key = normalizeLocaleKey(localeKey);
        LocaleBucket bucket = buckets.computeIfAbsent(key, ignored -> new LocaleBucket());
        long now = System.currentTimeMillis();
        if (bucket.snapshot != null && now < bucket.expireAt) {
            return bucket.snapshot;
        }
        try {
            Snapshot loaded = loader.get();
            bucket.snapshot = loaded == null ? Snapshot.empty() : loaded;
        } catch (Exception e) {
            log.warn("[CategoryTreeCache] 加载分类树快照失败 locale={}", key, e);
            if (bucket.snapshot == null) {
                bucket.snapshot = Snapshot.empty();
            }
        }
        bucket.expireAt = now + TTL_MS;
        return bucket.snapshot;
    }

    public synchronized void invalidate() {
        buckets.clear();
    }

    public void invalidateAndBroadcast() {
        invalidate();
        StringRedisTemplate redis = this.stringRedisTemplate;
        if (redis == null) {
            log.warn("[CategoryTreeCache] StringRedisTemplate 未注入，跨进程广播跳过（仅本地失效）");
            return;
        }
        try {
            redis.convertAndSend(INVALIDATE_CHANNEL, INSTANCE_ID);
            log.info("[CategoryTreeCache] 已广播缓存失效 channel={} from={}", INVALIDATE_CHANNEL, INSTANCE_ID);
        } catch (Exception e) {
            log.warn("[CategoryTreeCache] 广播缓存失效失败，已退化为仅本地失效（远端由 TTL 兜底）", e);
        }
    }

    /**
     * 返回整棵可见树的防御性拷贝（id=0 语义）。
     */
    public List<Tree<Integer>> copyAllTrees(Snapshot snapshot) {
        return copyForest(snapshot == null ? null : snapshot.fullTrees);
    }

    /**
     * 从快照中切片指定节点的直接子树；返回防御性拷贝。
     */
    public List<Tree<Integer>> sliceChildren(Snapshot snapshot, Integer categoryId) {
        if (snapshot == null || snapshot.fullTrees.isEmpty()) {
            return List.of();
        }
        if (categoryId == null || categoryId == 0) {
            return copyForest(snapshot.fullTrees);
        }
        Tree<Integer> node = snapshot.treeMap.get(categoryId);
        if (node == null) {
            return List.of();
        }
        List<Tree<Integer>> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        return copyForest(children);
    }

    @PostConstruct
    private void logBootstrap() {
        log.info("[CategoryTreeCache] 初始化完成 instanceId={} channel={}", INSTANCE_ID, INVALIDATE_CHANNEL);
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
        invalidate();
        log.info("[CategoryTreeCache] 收到缓存失效广播 from={} self={}, 已清空本地快照",
                from, INSTANCE_ID.equals(from));
    }

    public static String normalizeLocaleKey(String localeKey) {
        return localeKey == null ? "" : localeKey;
    }

    @SuppressWarnings("unchecked")
    static List<Tree<Integer>> copyForest(List<Tree<Integer>> forest) {
        if (forest == null || forest.isEmpty()) {
            return List.of();
        }
        List<Tree<Integer>> copies = new ArrayList<>(forest.size());
        for (Tree<Integer> node : forest) {
            copies.add(copyTree(node));
        }
        return copies;
    }

    @SuppressWarnings("unchecked")
    static Tree<Integer> copyTree(Tree<Integer> source) {
        if (source == null) {
            return null;
        }
        Tree<Integer> copy = new Tree<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if ("children".equals(key)) {
                Object value = entry.getValue();
                if (value instanceof List<?> list && !list.isEmpty()) {
                    List<Tree<Integer>> childCopies = new ArrayList<>(list.size());
                    for (Object item : list) {
                        childCopies.add(copyTree((Tree<Integer>) item));
                    }
                    copy.setChildren(childCopies);
                }
            } else {
                copy.put(key, entry.getValue());
            }
        }
        return copy;
    }

    private static final class LocaleBucket {
        Snapshot snapshot;
        long expireAt;
    }

    @Getter
    @AllArgsConstructor
    public static class Snapshot {

        private final List<Tree<Integer>> fullTrees;
        private final Map<Integer, Tree<Integer>> treeMap;

        public static Snapshot empty() {
            return new Snapshot(Collections.emptyList(), Collections.emptyMap());
        }
    }
}
