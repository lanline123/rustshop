// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author Jayce
 * @create 2024年11月22日 09:56
 */
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCache {

    private final RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 键   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 键      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key 键     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key 键     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 键 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        try {
            return operation.get(key);
        } catch (SerializationException e) {
            // 坏值（如缺少 @class 类型标识）无法反序列化。清掉该 key 让调用方回源重建，
            // 否则在 TTL 到期前每次读取都会抛异常，一个坏值足以打挂整条接口。
            log.warn("Redis 值反序列化失败，已清理该 key 并回退回源: {}", key, e);
            try {
                redisTemplate.delete(key);
            } catch (Exception ignore) {
                // 清理失败不影响回源重建
            }
            return null;
        }
    }

    /**
     * 批量获得缓存的基本对象（一次 MGET，避免 N 次往返）
     *
     * @param keys 键集合
     * @return 与 keys 顺序对应的值列表，未命中位置为 null
     */
    public <T> List<T> multiGetCacheObject(final Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 删除单个对象
     *
     * @param key 键
     */
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return long
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key 键      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 键 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key 键     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        for (T t : dataSet) {
            setOperation.add(t);
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key 键
     * @return <T>
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key 键
     * @param dataMap 缓存的数据
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key 键
     * @return <T>
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key 键   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key 键  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key 键
     * @param hkey Hash键
     */
    public void delCacheMapValue(final String key, final String hkey) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hkey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key 键   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 按模式删除 key，使用 SCAN 遍历，避免 KEYS 在集群或大键空间下的问题；SCAN 失败时回退为 KEYS。
     *
     * @param pattern 与 {@link #keys(String)} 相同的 glob 模式，如 {@code cat_floor*}
     * @return 删除的 key 数量
     */
    public long deleteKeysMatchingPattern(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return 0;
        }
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();
        long deleted = 0;
        try (Cursor<?> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                Object key = cursor.next();
                if (key != null && Boolean.TRUE.equals(redisTemplate.delete(key))) {
                    deleted++;
                }
            }
        } catch (Exception e) {
            Collection<String> fallbackKeys = redisTemplate.keys(pattern);
            if (fallbackKeys != null && !fallbackKeys.isEmpty()) {
                deleted = deleteObject(fallbackKeys);
            }
        }
        return deleted;
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key 锁的 key
     * @param value 锁的唯一值（建议用 UUID，防止误删别人的锁）
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return true 表示加锁成功，false 表示加锁失败
     */
    public boolean tryLock(final String key, final String value, final long timeout, final TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit));
    }

    /**
     * 释放锁（确保只删除自己加的锁）
     *
     * @param key 锁的 key
     * @param value 加锁时写入的唯一值
     */
    public void unlock(final String key, final String value) {
        // 使用 Lua 脚本防止误删
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(key), value);
    }
}
