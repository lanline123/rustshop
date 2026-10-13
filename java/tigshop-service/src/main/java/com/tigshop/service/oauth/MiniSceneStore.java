package com.tigshop.service.oauth;

import cn.hutool.json.JSONUtil;
import com.tigshop.bean.vo.common.MiniSceneVO;
import com.tigshop.common.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 小程序码 scene 短码映射（Redis）
 * <p>
 * 微信 getwxacodeunlimit 的 scene 最多 32 个可见字符；id 与 salesmanId 较长时无法直接拼接，
 * 将完整参数存入 Redis，scene 仅携带短码。
 */
@Component
@Slf4j
public class MiniSceneStore {

    private static final String CODE_KEY_PREFIX = "RUNNING:MINISCENE:CODE:";
    private static final String HASH_KEY_PREFIX = "RUNNING:MINISCENE:HASH:";
    private static final int TTL_SECONDS = 30 * 24 * 3600;
    private static final int CODE_LENGTH = 8;
    private static final int MAX_RETRY = 10;
    private static final char[] BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final SecureRandom random = new SecureRandom();

    @Resource
    private RedisCache redisCache;

    /**
     * 存储 scene 参数并返回短码；相同参数复用同一短码并刷新 TTL。
     */
    public String save(String path, Integer id, Integer salesmanId, Integer shopId) {
        MiniSceneVO data = new MiniSceneVO();
        data.setPath(path);
        data.setId(id);
        data.setSalesmanId(salesmanId);
        data.setShopId(shopId == null ? 0 : shopId);
        String payload = JSONUtil.toJsonStr(data);

        String hashKey = HASH_KEY_PREFIX + DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
        String existingCode = redisCache.getCacheObject(hashKey);
        if (existingCode != null && redisCache.getCacheObject(CODE_KEY_PREFIX + existingCode) != null) {
            redisCache.setCacheObject(CODE_KEY_PREFIX + existingCode, payload, TTL_SECONDS, TimeUnit.SECONDS);
            redisCache.expire(hashKey, TTL_SECONDS);
            return existingCode;
        }

        String code = generateUniqueCode();
        redisCache.setCacheObject(CODE_KEY_PREFIX + code, payload, TTL_SECONDS, TimeUnit.SECONDS);
        redisCache.setCacheObject(hashKey, code, TTL_SECONDS, TimeUnit.SECONDS);
        return code;
    }

    public MiniSceneVO resolve(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String payload = redisCache.getCacheObject(CODE_KEY_PREFIX + code.trim());
        if (payload == null || payload.isBlank()) {
            return null;
        }
        return JSONUtil.toBean(payload, MiniSceneVO.class);
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = randomCode();
            if (redisCache.getCacheObject(CODE_KEY_PREFIX + code) == null) {
                return code;
            }
        }
        throw new IllegalStateException("生成小程序码短码失败，请重试");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62[random.nextInt(BASE62.length)]);
        }
        return sb.toString();
    }
}
