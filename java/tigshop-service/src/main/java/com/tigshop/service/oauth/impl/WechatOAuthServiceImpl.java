// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.oauth.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.GetWechatCodeParam;
import com.tigshop.bean.vo.common.MiniSceneVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.MiniSceneStore;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信实现
 *
 * @author Tigshop团队
 * @create 2025年01月23日 17:08
 */
@Slf4j
@Service
public class WechatOAuthServiceImpl implements WechatOAuthService {

    private static final String TOKEN_KEY_MP = "wechat:token:mp";
    private static final String TOKEN_KEY_MINI = "wechat:token:mini";
    private static final String TOKEN_KEY_LIVE_MINI = "wechat:token:live_mini";
    private static final int TOKEN_SAFETY_MARGIN_SECONDS = 300;

    @Resource
    private WechatApiClient wechatApiClient;

    @Resource
    private ConfigService configService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private MiniSceneStore miniSceneStore;

    @Override
    public String getMiniCode(String path, Integer productId) {
        return getMiniCode(path, productId, null);
    }

    @Override
    public String getMiniCode(String path, Integer productId, Integer salesmanId) {
        return getMiniCode(path, productId, salesmanId, null);
    }

    @Override
    public String getMiniCode(String path, Integer productId, Integer salesmanId, Integer shopId) {
        try {
            return doGetMiniCode(path, productId, salesmanId, shopId, false);
        } catch (WechatTokenInvalidException e) {
            log.warn("微信小程序 token 已失效，强制刷新后重试: {}", e.getMessage());
            return doGetMiniCode(path, productId, salesmanId, shopId, true);
        }
    }

    @Override
    public MiniSceneVO resolveMiniScene(String scene) {
        return miniSceneStore.resolve(scene);
    }

    private String doGetMiniCode(String path, Integer productId, Integer salesmanId, Integer shopId, boolean forceRefresh) {
        // 注意：获取小程序码必须使用“小程序”的 access_token，而不是公众号
        String accessToken = forceRefresh
                ? refreshMiniAccessToken()
                : getMiniAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw new GlobalException("获取小程序access_token失败");
        }

        // getwxacodeunlimit: page 只能是页面路径，query 通过 scene 传递
        String page = normalizeMiniPage(path);
        String scene = buildMiniScene(path, productId, salesmanId, shopId);
        GetWechatCodeParam param = new GetWechatCodeParam(scene, page);
        // check_path=true 只校验「已发布线上正式版」是否含该 page；拼团等分包页未随正式版发布时会 41030。
        // 关闭校验后仍可生成码，扫码时以小程序当前已发布版本为准跳转。
        param.setCheck_path(Boolean.FALSE);

        ResponseEntity<byte[]> resp = wechatApiClient.getWechatCode(accessToken, param);
        byte[] body = resp.getBody();
        if (body == null || body.length == 0) {
            throw new GlobalException("获取小程序码失败：微信返回空响应");
        }

        // 微信错误时会返回 JSON：{"errcode":xxx,"errmsg":"..."}
        MediaType contentType = resp.getHeaders().getContentType();
        if ((contentType != null && MediaType.APPLICATION_JSON.includes(contentType)) || isLikelyJson(body)) {
            String json = new String(body, StandardCharsets.UTF_8);
            Integer errcode = parseErrcode(json);
            if (!forceRefresh && isTokenInvalidErrcode(errcode)) {
                // 抛特定异常让上层捕获并重试
                throw new WechatTokenInvalidException(StrUtil.format("token失效({}): {}", errcode, json));
            }
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = JSONUtil.toBean(json, Map.class);
                Object errmsg = map.get("errmsg");
                throw new GlobalException(StrUtil.format("获取小程序码失败：{}({})", errmsg, errcode));
            } catch (GlobalException | WechatTokenInvalidException ge) {
                throw ge;
            } catch (Exception ignore) {
                throw new GlobalException("获取小程序码失败：" + json);
            }
        }

        String mime = guessImageMimeType(contentType, body);
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(body);
    }

    /**
     * 微信 token 已失效的几个错误码：
     * 40001 - access_token 无效或非最新
     * 40014 - 不合法的 access_token
     * 42001 - access_token 超时
     */
    private static boolean isTokenInvalidErrcode(Integer errcode) {
        return errcode != null && (errcode == 40001 || errcode == 40014 || errcode == 42001);
    }

    @SuppressWarnings("unchecked")
    private static Integer parseErrcode(String json) {
        try {
            Map<String, Object> map = JSONUtil.toBean(json, Map.class);
            Object errcode = map.get("errcode");
            if (errcode instanceof Number) {
                return ((Number) errcode).intValue();
            }
            if (errcode != null) {
                return Integer.parseInt(errcode.toString());
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * 内部异常：标识微信 token 失效，触发上层 refresh 重试
     */
    private static class WechatTokenInvalidException extends RuntimeException {
        WechatTokenInvalidException(String message) {
            super(message);
        }
    }

    /**
     * 构建小程序 scene：优先紧凑格式 id=xxx&s=yyy&shopId=zzz；超过 32 字符时用 Redis 短码。
     * <p>
     * salesmanId / shopId 缺省或 ≤0 时不写入 scene（缺省即自营 shopId=0），保持旧调用方行为不变。
     */
    private String buildMiniScene(String path, Integer productId, Integer salesmanId, Integer shopId) {
        boolean hasSalesman = salesmanId != null && salesmanId > 0;
        boolean hasShop = shopId != null && shopId > 0;
        if (!hasSalesman && !hasShop) {
            return StrUtil.format("id={}", productId);
        }
        StringBuilder sb = new StringBuilder(StrUtil.format("id={}", productId));
        if (hasSalesman) {
            sb.append("&s=").append(salesmanId);
        }
        if (hasShop) {
            sb.append("&shopId=").append(shopId);
        }
        String directScene = sb.toString();
        if (directScene.length() <= 32) {
            return directScene;
        }
        return miniSceneStore.save(path, productId, salesmanId, shopId);
    }

    private static String normalizeMiniPage(String path) {
        if (StrUtil.isBlank(path)) {
            return null;
        }
        String p = path.trim();
        int idx = p.indexOf('?');
        if (idx >= 0) {
            p = p.substring(0, idx);
        }
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return StrUtil.blankToDefault(p, null);
    }

    private static boolean isLikelyJson(byte[] body) {
        if (body == null || body.length == 0) {
            return false;
        }
        int i = 0;
        while (i < body.length && (body[i] == ' ' || body[i] == '\n' || body[i] == '\r' || body[i] == '\t')) {
            i++;
        }
        return i < body.length && body[i] == '{';
    }

    private static String guessImageMimeType(MediaType contentType, byte[] body) {
        if (contentType != null) {
            if (MediaType.IMAGE_PNG.includes(contentType)) {
                return "image/png";
            }
            if (MediaType.IMAGE_JPEG.includes(contentType)) {
                return "image/jpeg";
            }
        }
        if (isPng(body)) {
            return "image/png";
        }
        if (isJpeg(body)) {
            return "image/jpeg";
        }
        // 微信一般返回 jpeg/png，兜底 jpeg
        return "image/jpeg";
    }

    private static boolean isPng(byte[] b) {
        return b != null
                && b.length >= 8
                && (b[0] & 0xFF) == 0x89
                && b[1] == 0x50
                && b[2] == 0x4E
                && b[3] == 0x47
                && b[4] == 0x0D
                && b[5] == 0x0A
                && b[6] == 0x1A
                && b[7] == 0x0A;
    }

    private static boolean isJpeg(byte[] b) {
        return b != null
                && b.length >= 3
                && (b[0] & 0xFF) == 0xFF
                && (b[1] & 0xFF) == 0xD8
                && (b[2] & 0xFF) == 0xFF;
    }

    @Override
    public String getMiniCodeByFullPath(String fullPath) {
        String accessToken = getMiniAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw new GlobalException("获取小程序access_token失败");
        }

        String page = null;
        String scene = "home";
        if (StrUtil.isNotBlank(fullPath)) {
            String p = fullPath.trim();
            while (p.startsWith("/")) {
                p = p.substring(1);
            }
            int qIdx = p.indexOf('?');
            if (qIdx >= 0) {
                page = p.substring(0, qIdx);
                scene = p.substring(qIdx + 1);
                if (scene.length() > 32) {
                    scene = scene.substring(0, 32);
                }
            } else {
                page = p;
            }
            if (StrUtil.isBlank(page)) {
                page = null;
            }
        }
        if (StrUtil.isBlank(scene)) {
            scene = "home";
        }

        GetWechatCodeParam param = new GetWechatCodeParam(scene, page);
        // 与 doGetMiniCode 一致：避免未发布正式版页面触发 41030
        param.setCheck_path(Boolean.FALSE);

        ResponseEntity<byte[]> resp = wechatApiClient.getWechatCode(accessToken, param);
        byte[] body = resp.getBody();
        if (body == null || body.length == 0) {
            throw new GlobalException("获取小程序码失败：微信返回空响应");
        }

        MediaType contentType = resp.getHeaders().getContentType();
        if ((contentType != null && MediaType.APPLICATION_JSON.includes(contentType)) || isLikelyJson(body)) {
            String json = new String(body, StandardCharsets.UTF_8);
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = JSONUtil.toBean(json, Map.class);
                Object errmsg = map.get("errmsg");
                Object errcode = map.get("errcode");
                throw new GlobalException(StrUtil.format("获取小程序码失败：{}({})", errmsg, errcode));
            } catch (GlobalException ge) {
                throw ge;
            } catch (Exception ignore) {
                throw new GlobalException("获取小程序码失败：" + json);
            }
        }

        String mime = guessImageMimeType(contentType, body);
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(body);
    }

    @Override
    public String getMpAccessToken() {
        return getAccessTokenCached(TOKEN_KEY_MP,
                SettingsEnum.WECHAT_APP_ID, SettingsEnum.WECHAT_APP_SECRET, false);
    }

    @Override
    public String getMiniAccessToken() {
        return getAccessTokenCached(TOKEN_KEY_MINI,
                SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID, SettingsEnum.WECHAT_MINI_PROGRAM_SECRET, false);
    }

    @Override
    public String getLiveMiniAccessToken() {
        return getAccessTokenCached(TOKEN_KEY_LIVE_MINI,
                SettingsEnum.WECHAT_LIVE_MINI_APP_ID, SettingsEnum.WECHAT_LIVE_MINI_APP_SECRET, false);
    }

    @Override
    public String refreshMpAccessToken() {
        return getAccessTokenCached(TOKEN_KEY_MP,
                SettingsEnum.WECHAT_APP_ID, SettingsEnum.WECHAT_APP_SECRET, true);
    }

    @Override
    public String refreshMiniAccessToken() {
        return getAccessTokenCached(TOKEN_KEY_MINI,
                SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID, SettingsEnum.WECHAT_MINI_PROGRAM_SECRET, true);
    }

    @Override
    public String refreshLiveMiniAccessToken() {
        return getAccessTokenCached(TOKEN_KEY_LIVE_MINI,
                SettingsEnum.WECHAT_LIVE_MINI_APP_ID, SettingsEnum.WECHAT_LIVE_MINI_APP_SECRET, true);
    }

    private String getAccessTokenCached(String cacheKey, SettingsEnum appIdEnum, SettingsEnum appSecretEnum, boolean forceRefresh) {
        if (!forceRefresh) {
            String cached = redisCache.getCacheObject(cacheKey);
            if (StrUtil.isNotBlank(cached)) {
                return cached;
            }
        }
        synchronized (this) {
            if (!forceRefresh) {
                String cached = redisCache.getCacheObject(cacheKey);
                if (StrUtil.isNotBlank(cached)) {
                    return cached;
                }
            } else {
                // 强制刷新场景：先删除旧缓存避免其他线程读到失效 token
                redisCache.deleteObject(cacheKey);
            }
            String appId = configService.getConfigByCode(appIdEnum.getBizCode()).getBizVal();
            String appSecret = configService.getConfigByCode(appSecretEnum.getBizCode()).getBizVal();
            if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
                log.warn("微信配置缺失: {}/{}", appIdEnum.getBizCode(), appSecretEnum.getBizCode());
                return null;
            }
            try {
                GetTokenResult result = wechatApiClient.getToken("client_credential", appId, appSecret);
                if (result == null || StrUtil.isBlank(result.getAccessToken())) {
                    log.warn("微信 getToken 返回空: cacheKey={}", cacheKey);
                    return null;
                }
                int expiresIn = result.getExpiresIn() != null ? result.getExpiresIn() : 7200;
                int ttl = Math.max(60, expiresIn - TOKEN_SAFETY_MARGIN_SECONDS);
                redisCache.setCacheObject(cacheKey, result.getAccessToken(), ttl, TimeUnit.SECONDS);
                return result.getAccessToken();
            } catch (Exception e) {
                log.warn("微信 getToken 失败: cacheKey={}, error={}", cacheKey, e.getMessage());
                return null;
            }
        }
    }
}