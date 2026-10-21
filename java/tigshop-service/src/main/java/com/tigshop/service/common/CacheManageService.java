package com.tigshop.service.common;

/**
 * 缓存管理 服务接口
 *
 * @author kidd
 * @since 2025/4/8 16:10
 */
public interface CacheManageService {

    /**
     * 清除缓存
     */
    void cleanup(String tag);
}
