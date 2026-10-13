// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 微信公众号用户昵称缓存。
 * <p>
 * 用于在自动回复 / 关键字回复渲染阶段把 {@code {nickname}} 占位符替换为用户昵称。
 * <ul>
 *   <li>缓存键：{@value #KEY_PREFIX}{openid}</li>
 *   <li>TTL：10 分钟</li>
 *   <li>失败兜底：返回 {@value #FALLBACK}，且<strong>不写缓存</strong>，避免错误结果被长期缓存</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WxUserInfoCache {

    private static final String KEY_PREFIX = "wechat:mp:nickname:";
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final String FALLBACK = "朋友";

    private final RedisTemplate<String, String> redisTemplate;

    @Resource
    private WxMpServiceHolder wxMpServiceHolder;

    /**
     * 获取昵称：先查 Redis，命中即返；未命中则调用微信接口拉取后写入缓存。
     * 任何异常或拿不到非空昵称时一律返回 {@value #FALLBACK}，且不写缓存。
     */
    public String getNickname(String openid) {
        if (StrUtil.isBlank(openid)) {
            return FALLBACK;
        }
        String key = KEY_PREFIX + openid;

        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(cached)) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("读取微信昵称缓存失败，openid={}", openid, e);
        }

        try {
            WxMpService wxMpService = wxMpServiceHolder.get();
            if (wxMpService == null) {
                return FALLBACK;
            }
            WxMpUser user = wxMpService.getUserService().userInfo(openid, null);
            if (user != null && StrUtil.isNotBlank(user.getNickname())) {
                String nickname = user.getNickname();
                try {
                    redisTemplate.opsForValue().set(key, nickname, TTL);
                } catch (Exception e) {
                    log.warn("写入微信昵称缓存失败，openid={}, nickname={}", openid, nickname, e);
                }
                return nickname;
            }
        } catch (Exception e) {
            log.warn("调用微信用户信息接口失败，openid={}", openid, e);
        }
        return FALLBACK;
    }
}
