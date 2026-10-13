// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.lang.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.lang.ImageI18n;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.mapper.lang.ImageI18nMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.lang.ImageI18nService;
import com.tigshop.service.lang.LocalesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 海外站图片本地化映射服务
 *
 * @author Tigshop团队
 */
@Service
public class ImageI18nServiceImpl extends BaseServiceImpl<ImageI18nMapper, ImageI18n> implements ImageI18nService {

    /** 图片本地化缓存前缀（共享缓存，多实例一致） */
    private static final String CACHE_PREFIX = "image_i18n::";
    /** 语言代码 -> 语言id 缓存前缀 */
    private static final String LOCALE_PREFIX = "image_i18n_locale::";
    /** 负缓存哨兵值：记录"该图无本地化"，避免重复回源 */
    private static final String NULL_SENTINEL = "__IMG_I18N_NULL__";
    /** 命中缓存 TTL（秒）：1 天；写入走 upsert 会主动清键，所以可放长 */
    private static final int HIT_TTL = 86400;
    /** 负缓存 TTL（秒）：10 分钟，平衡未命中保护与新配生效延迟 */
    private static final int NEG_TTL = 600;
    /** 语言映射 TTL（秒）：1 小时 */
    private static final int LOCALE_TTL = 3600;

    @Resource
    private RedisCache redisCache;
    @Resource
    private LocalesService localesService;

    @Override
    public String resolve(String localeCode, Integer dataType, String url) {
        if (StrUtil.isBlank(url) || StrUtil.isBlank(localeCode) || dataType == null) {
            return url;
        }
        String md5 = SecureUtil.md5(url);
        String cacheKey = cacheKey(localeCode, dataType, md5);
        String cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            return NULL_SENTINEL.equals(cached) ? url : cached;
        }
        Integer localeId = resolveLocaleId(localeCode);
        if (localeId == null) {
            return url;
        }
        ImageI18n hit = this.getOne(new LambdaQueryWrapper<ImageI18n>()
                .eq(ImageI18n::getLocaleId, localeId)
                .eq(ImageI18n::getDataType, dataType)
                .eq(ImageI18n::getOriginalKey, md5)
                .last("limit 1"));
        if (hit != null && StrUtil.isNotBlank(hit.getLocalizedUrl())) {
            redisCache.setCacheObject(cacheKey, hit.getLocalizedUrl(), HIT_TTL, TimeUnit.SECONDS);
            return hit.getLocalizedUrl();
        }
        redisCache.setCacheObject(cacheKey, NULL_SENTINEL, NEG_TTL, TimeUnit.SECONDS);
        return url;
    }

    @Override
    public void warmBatch(String localeCode, Integer dataType, Collection<String> urls) {
        if (StrUtil.isBlank(localeCode) || dataType == null || urls == null || urls.isEmpty()) {
            return;
        }
        // 去重 + 过滤空值，保持顺序
        Set<String> distinct = new LinkedHashSet<>();
        for (String u : urls) {
            if (StrUtil.isNotBlank(u)) {
                distinct.add(u);
            }
        }
        if (distinct.isEmpty()) {
            return;
        }
        // url -> md5
        Map<String, String> urlToMd5 = new HashMap<>(distinct.size());
        List<String> cacheKeys = new ArrayList<>(distinct.size());
        List<String> orderedUrls = new ArrayList<>(distinct);
        for (String u : orderedUrls) {
            String md5 = SecureUtil.md5(u);
            urlToMd5.put(u, md5);
            cacheKeys.add(cacheKey(localeCode, dataType, md5));
        }
        // 一次 MGET，跳过已缓存（命中或负缓存）的 url
        List<String> cachedValues = redisCache.multiGetCacheObject(cacheKeys);
        List<String> missUrls = new ArrayList<>();
        List<String> missMd5 = new ArrayList<>();
        for (int i = 0; i < orderedUrls.size(); i++) {
            String v = (cachedValues != null && i < cachedValues.size()) ? cachedValues.get(i) : null;
            if (v == null) {
                missUrls.add(orderedUrls.get(i));
                missMd5.add(urlToMd5.get(orderedUrls.get(i)));
            }
        }
        if (missUrls.isEmpty()) {
            return;
        }
        Integer localeId = resolveLocaleId(localeCode);
        if (localeId == null) {
            return;
        }
        // 一次 IN 查询所有未缓存的 key
        List<ImageI18n> rows = this.list(new LambdaQueryWrapper<ImageI18n>()
                .eq(ImageI18n::getLocaleId, localeId)
                .eq(ImageI18n::getDataType, dataType)
                .in(ImageI18n::getOriginalKey, missMd5));
        Map<String, String> md5ToValue = new HashMap<>(rows.size());
        for (ImageI18n row : rows) {
            if (StrUtil.isNotBlank(row.getLocalizedUrl())) {
                md5ToValue.put(row.getOriginalKey(), row.getLocalizedUrl());
            }
        }
        // 回填缓存：命中写值（长 TTL），未命中写负缓存（短 TTL）
        for (String u : missUrls) {
            String md5 = urlToMd5.get(u);
            String value = md5ToValue.get(md5);
            String key = cacheKey(localeCode, dataType, md5);
            if (value != null) {
                redisCache.setCacheObject(key, value, HIT_TTL, TimeUnit.SECONDS);
            } else {
                redisCache.setCacheObject(key, NULL_SENTINEL, NEG_TTL, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void upsert(Integer localeId, String localeCode, Integer dataType, String originalUrl, String localizedUrl) {
        if (localeId == null || dataType == null || StrUtil.isBlank(originalUrl)) {
            return;
        }
        String md5 = SecureUtil.md5(originalUrl);
        ImageI18n existing = this.getOne(new LambdaQueryWrapper<ImageI18n>()
                .eq(ImageI18n::getLocaleId, localeId)
                .eq(ImageI18n::getDataType, dataType)
                .eq(ImageI18n::getOriginalKey, md5)
                .last("limit 1"));
        if (existing != null) {
            existing.setOriginalUrl(originalUrl);
            existing.setLocalizedUrl(localizedUrl);
            this.updateById(existing);
        } else {
            this.save(ImageI18n.builder()
                    .localeId(localeId)
                    .dataType(dataType)
                    .originalKey(md5)
                    .originalUrl(originalUrl)
                    .localizedUrl(localizedUrl)
                    .addTime((int) (System.currentTimeMillis() / 1000))
                    .build());
        }
        // 清缓存（含负缓存），多实例下写入即时生效
        if (StrUtil.isNotBlank(localeCode)) {
            redisCache.deleteObject(cacheKey(localeCode, dataType, md5));
        }
    }

    private String cacheKey(String localeCode, Integer dataType, String md5) {
        return CACHE_PREFIX + localeCode + ":" + dataType + ":" + md5;
    }

    private Integer resolveLocaleId(String localeCode) {
        String key = LOCALE_PREFIX + localeCode;
        Integer cached = redisCache.getCacheObject(key);
        if (cached != null) {
            return cached;
        }
        Locales locales = localesService.getOne(new LambdaQueryWrapper<Locales>()
                .eq(Locales::getLocaleCode, localeCode)
                .last("limit 1"));
        if (locales == null) {
            return null;
        }
        redisCache.setCacheObject(key, locales.getId(), LOCALE_TTL, TimeUnit.SECONDS);
        return locales.getId();
    }
}
