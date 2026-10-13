package com.tigshop.service.decorate;

import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.common.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 装修离散组件 {@code DecorateDiscreteServiceImpl.detail} 的进程内快照缓存
 * （5 分钟 TTL 兜底 + Redis Pub/Sub 跨进程失效）。
 *
 * <p>移动端导航/个人中心等离散组件按 {@code decorateSn} 单查无缓存，海外版还会逐字段翻译，是热路径。
 * 设计参照 {@link HomeDecorateCache}：进程内缓存已翻译好的 {@link DecorateDiscreteVO}（以不可变 JSON 保存，
 * 每次读取反序列化出独立副本，防御 {@code SeoResponseAdvice}/下游就地修改污染快照），离散组件保存后广播全量失效。
 */
@Component
@Slf4j
public class DecorateDiscreteCache implements MessageListener {

    public static final String INVALIDATE_CHANNEL = "decorate:discrete:invalidate";

    private static final String INSTANCE_ID =
            ManagementFactory.getRuntimeMXBean().getName() + "#" + UUID.randomUUID();

    /** 缓存 TTL 兜底：5 分钟 */
    private static final long TTL_MS = 5 * 60 * 1000L;

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    /**
     * 读取离散组件详情；命中且未过期复用缓存 JSON，否则通过 loader 重建并写入缓存。
     * 无论命中与否都反序列化出独立副本返回（{@code null} 也可缓存）。
     */
    public DecorateDiscreteVO get(String key, Supplier<DecorateDiscreteVO> loader) {
        long now = System.currentTimeMillis();
        Entry entry = cache.get(key);
        String json;
        if (entry != null && entry.expireAt > now) {
            json = entry.json;
        } else {
            DecorateDiscreteVO loaded = loader.get();
            json = JsonUtil.toJson(loaded);
            cache.put(key, new Entry(json, now + TTL_MS));
        }
        return JsonUtil.fromJson(json, DecorateDiscreteVO.class);
    }

    /**
     * 失效本地全部离散组件缓存，并广播到其它进程。
     */
    public void invalidateAndBroadcast() {
        cache.clear();
        StringRedisTemplate redis = this.stringRedisTemplate;
        if (redis == null) {
            log.warn("[DecorateDiscreteCache] StringRedisTemplate 未注入，跨进程广播跳过（仅本地失效）");
            return;
        }
        try {
            redis.convertAndSend(INVALIDATE_CHANNEL, "*");
            log.info("[DecorateDiscreteCache] 已广播离散组件缓存失效 channel={} from={}", INVALIDATE_CHANNEL, INSTANCE_ID);
        } catch (Exception e) {
            log.warn("[DecorateDiscreteCache] 广播离散组件缓存失效失败，已退化为仅本地失效（远端由 TTL 兜底）", e);
        }
    }

    @PostConstruct
    private void logBootstrap() {
        log.info("[DecorateDiscreteCache] 初始化完成 instanceId={} channel={}", INSTANCE_ID, INVALIDATE_CHANNEL);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        cache.clear();
        log.info("[DecorateDiscreteCache] 收到离散组件缓存失效广播，已清空本地缓存");
    }

    private static final class Entry {
        private final String json;
        private final long expireAt;

        private Entry(String json, long expireAt) {
            this.json = json != null ? json : "null";
            this.expireAt = expireAt;
        }
    }
}
