package com.tigshop.service.setting;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 配置项 {@code getConfigVal} 的进程内缓存（5 分钟 TTL 兜底 + Redis Pub/Sub 跨进程失效）。
 *
 * <p>设计参照 {@link com.tigshop.service.product.CategoryTreeCache}：进程内 {@link ConcurrentHashMap}
 * 缓存配置值，配置保存后按 bizCode 广播失效，tigshop-adminapi ↔ tigshop-api 各 JVM 立即清除对应缓存，
 * 避免后台改配置后 C 端最长 5 分钟才生效。Redis 不可用时退化为仅本地失效，由 TTL 兜底。
 */
@Component
@Slf4j
public class ConfigValCache implements MessageListener {

    public static final String INVALIDATE_CHANNEL = "config:val:invalidate";

    /** 广播全部失效的特殊标记 */
    private static final String CLEAR_ALL = "*";

    private static final String INSTANCE_ID =
            ManagementFactory.getRuntimeMXBean().getName() + "#" + UUID.randomUUID();

    /** 缓存 TTL 兜底：5 分钟 */
    private static final long TTL_MS = 5 * 60 * 1000L;

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    /**
     * 读取配置值；命中且未过期直接返回，否则通过 loader 重建并写入缓存。
     */
    public String get(String bizCode, Supplier<String> loader) {
        long now = System.currentTimeMillis();
        Entry entry = cache.get(bizCode);
        if (entry != null && entry.expireAt > now) {
            return entry.value;
        }
        String value = loader.get();
        cache.put(bizCode, new Entry(value, now + TTL_MS));
        return value;
    }

    /**
     * 判断指定 bizCode 当前是否已缓存且未过期（用于批量预载的哨兵判断）。
     */
    public boolean isCached(String bizCode) {
        Entry entry = cache.get(bizCode);
        return entry != null && entry.expireAt > System.currentTimeMillis();
    }

    /**
     * 批量预载配置值：一次性写入多个 bizCode→value，统一附 TTL。
     * 用于 {@code getBase} 等串行读取大量配置的冷启动场景，把 N 次 DB 往返降为 1 次。
     */
    public void warmUp(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        long expireAt = System.currentTimeMillis() + TTL_MS;
        values.forEach((bizCode, value) -> {
            if (bizCode != null) {
                cache.put(bizCode, new Entry(value, expireAt));
            }
        });
    }

    /**
     * 失效指定 bizCode 的本地缓存，并广播到其它进程。
     */
    public void invalidateAndBroadcast(Collection<String> bizCodes) {
        if (bizCodes == null || bizCodes.isEmpty()) {
            return;
        }
        bizCodes.forEach(cache::remove);
        StringRedisTemplate redis = this.stringRedisTemplate;
        if (redis == null) {
            log.warn("[ConfigValCache] StringRedisTemplate 未注入，跨进程广播跳过（仅本地失效）");
            return;
        }
        try {
            redis.convertAndSend(INVALIDATE_CHANNEL, String.join(",", bizCodes));
            log.info("[ConfigValCache] 已广播配置失效 channel={} codes={} from={}",
                    INVALIDATE_CHANNEL, bizCodes, INSTANCE_ID);
        } catch (Exception e) {
            log.warn("[ConfigValCache] 广播配置失效失败，已退化为仅本地失效（远端由 TTL 兜底）", e);
        }
    }

    @PostConstruct
    private void logBootstrap() {
        log.info("[ConfigValCache] 初始化完成 instanceId={} channel={}", INSTANCE_ID, INVALIDATE_CHANNEL);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body;
        try {
            body = message.getBody() == null
                    ? ""
                    : new String(message.getBody(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            body = "";
        }
        if (body.isEmpty() || CLEAR_ALL.equals(body)) {
            cache.clear();
            log.info("[ConfigValCache] 收到全量失效广播，已清空本地配置缓存");
            return;
        }
        for (String bizCode : body.split(",")) {
            if (!bizCode.isEmpty()) {
                cache.remove(bizCode);
            }
        }
        log.info("[ConfigValCache] 收到配置失效广播 codes={}，已清除对应本地缓存", body);
    }

    private static final class Entry {
        private final String value;
        private final long expireAt;

        private Entry(String value, long expireAt) {
            this.value = value != null ? value : "";
            this.expireAt = expireAt;
        }
    }
}
