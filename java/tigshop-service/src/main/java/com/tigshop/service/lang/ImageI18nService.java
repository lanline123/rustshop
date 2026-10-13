// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.lang;

import com.tigshop.bean.model.lang.ImageI18n;
import com.tigshop.service.common.BaseService;

import java.util.Collection;

/**
 * 海外站图片本地化映射服务
 * <p>
 * 取代「图片 URL 当翻译 key 塞进 translations_data」的滥用：单表查询 + 共享缓存 + 负缓存 + 批量预热，
 * 消除商品详情/列表图片翻译的 N+1 与 99% 未命中带来的 SQL/日志风暴。
 *
 * @author Tigshop团队
 */
public interface ImageI18nService extends BaseService<ImageI18n> {

    /**
     * 解析单张图片 URL 的本地化结果。
     * <p>命中返回本地化 URL；未命中（含负缓存）返回原 URL。仅海外站生效。
     *
     * @param localeCode 语言代码
     * @param dataType   数据类型：7=商品图片 8=规格图片
     * @param url        原图 URL（key，与历史 translations_data.translation_name 语义一致）
     * @return 本地化 URL 或原 URL
     */
    String resolve(String localeCode, Integer dataType, String url);

    /**
     * 批量预热：一次 MGET + 一次 IN 查询，把整页/整商品的图片本地化结果写入共享缓存，
     * 供随后的 {@code @JsonTranslate} 序列化逐字段命中缓存（0 SQL）。
     *
     * @param localeCode 语言代码
     * @param dataType   数据类型：7=商品图片 8=规格图片
     * @param urls       原图 URL 集合
     */
    void warmBatch(String localeCode, Integer dataType, Collection<String> urls);

    /**
     * 写入/更新一条图片本地化映射，并清除对应缓存键（含负缓存），保证多实例即时生效。
     *
     * @param localeId     语言 id
     * @param localeCode   语言代码（用于清缓存）
     * @param dataType     数据类型：7=商品图片 8=规格图片
     * @param originalUrl  原图 URL
     * @param localizedUrl 本地化后的图 URL
     */
    void upsert(Integer localeId, String localeCode, Integer dataType, String originalUrl, String localizedUrl);
}
