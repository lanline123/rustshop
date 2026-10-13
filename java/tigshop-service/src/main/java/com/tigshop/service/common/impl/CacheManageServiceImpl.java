package com.tigshop.service.common.impl;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.common.constant.Constants;
import com.tigshop.service.common.CacheManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缓存管理 服务实现
 *
 * @author kidd
 * @since 2025/4/8 16:10
 */
@RequiredArgsConstructor
@Service
public class CacheManageServiceImpl implements CacheManageService {

    private static final List<String> NO_CLEANUP_TAGS = List.of(Constants.ADMIN_TOKEN, Constants.ADMIN_USER);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void cleanup(String tag) {
        // 获取所有 key（注意：keys * 会匹配所有 key，适合开发环境，慎用于线上大数据量）
        Set<String> allKeys = redisTemplate.keys("*");

        if (CollUtil.isNotEmpty(allKeys)) {
            // 过滤掉你想保留的 key
            Set<String> keysToDelete = allKeys.stream()
                    .filter(key -> NO_CLEANUP_TAGS.stream().noneMatch(key::startsWith))
                    .collect(Collectors.toSet());

            // 批量删除剩下的 key
            if (CollUtil.isNotEmpty(keysToDelete)) {
                redisTemplate.delete(keysToDelete);
                System.out.println("Deleted keys: " + keysToDelete);
            } else {
                System.out.println("No keys to delete.");
            }
        } else {
            System.out.println("Redis is empty.");
        }
    }
}
