package com.tigshop.service.decorate;

import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
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
 * 首页装修读取链路（{@code /api/home/home/index}、{@code /pcIndex} 及衍生页）的进程内快照缓存
 * （5 分钟 TTL 兜底 + Redis Pub/Sub 跨进程失效）。
 *
 * <p>设计参照 {@link com.tigshop.service.product.CategoryTreeCache} 与
 * {@link com.tigshop.service.setting.ConfigValCache}：进程内 {@link ConcurrentHashMap} 缓存已组装好的
 * {@link DecorateModuleListVO}（含 catNav navList），装修/分类栏保存后广播全量失效，
 * tigshop-adminapi ↔ tigshop-api 各 JVM 立即清除缓存，避免后台改装修后 C 端最长 5 分钟才生效。
 * Redis 不可用时退化为仅本地失效，由 TTL 兜底。
 *
 * <p><b>正确性约束（防御性拷贝）</b>：缓存内部以不可变 JSON 字符串保存快照，每次读取都反序列化出
 * 独立对象返回，避免 {@code SeoResponseAdvice}/下游对返回 VO 的就地修改污染共享快照
 * （同 {@link com.tigshop.service.product.CategoryTreeCache} 的教训）。
 */
@Component
@Slf4j
public class HomeDecorateCache implements MessageListener {

    public static final String INVALIDATE_CHANNEL = "home:decorate:invalidate";

    private static final String INSTANCE_ID =
            ManagementFactory.getRuntimeMXBean().getName() + "#" + UUID.randomUUID();

    /** 缓存 TTL 兜底：5 分钟（分类/品牌名等关联数据变更的陈旧由该 TTL 兜底） */
    private static final long TTL_MS = 5 * 60 * 1000L;

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    /**
     * 读取首页装修模块；命中且未过期复用缓存 JSON，否则通过 loader 重建并写入缓存。
     * 无论命中与否都反序列化出独立副本返回，保证返回对象结构一致且不共享可变状态。
     */
    public DecorateModuleListVO get(String key, Supplier<DecorateModuleListVO> loader) {
        long now = System.currentTimeMillis();
        Entry entry = cache.get(key);
        String json;
        if (entry != null && entry.expireAt > now) {
            json = entry.json;
        } else {
            // loader 抛异常（如模板不存在）时不写缓存，异常直接向上抛出
            DecorateModuleListVO loaded = loader.get();
            json = JsonUtil.toJson(loaded);
            cache.put(key, new Entry(json, now + TTL_MS));
        }
        return JsonUtil.fromJson(json, DecorateModuleListVO.class);
    }

    /**
     * 失效本地全部首页装修缓存，并广播到其它进程。
     */
    public void invalidateAndBroadcast() {
        cache.clear();
        StringRedisTemplate redis = this.stringRedisTemplate;
        if (redis == null) {
            log.warn("[HomeDecorateCache] StringRedisTemplate 未注入，跨进程广播跳过（仅本地失效）");
            return;
        }
        try {
            redis.convertAndSend(INVALIDATE_CHANNEL, "*");
            log.info("[HomeDecorateCache] 已广播首页装修缓存失效 channel={} from={}", INVALIDATE_CHANNEL, INSTANCE_ID);
        } catch (Exception e) {
            log.warn("[HomeDecorateCache] 广播首页装修缓存失效失败，已退化为仅本地失效（远端由 TTL 兜底）", e);
        }
    }

    @PostConstruct
    private void logBootstrap() {
        log.info("[HomeDecorateCache] 初始化完成 instanceId={} channel={}", INSTANCE_ID, INVALIDATE_CHANNEL);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        cache.clear();
        log.info("[HomeDecorateCache] 收到首页装修缓存失效广播，已清空本地缓存");
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
