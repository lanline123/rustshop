package com.tigshop.service.promotion.impl;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.common.shareTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.ShortLinkParam;
import com.tigshop.bean.feign.wechat.ShortLinkResult;
import com.tigshop.bean.vo.decorate.DecoratePromoteVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.promotion.ShortUrlService;
import com.tigshop.mapper.promotion.ShortUrlMapper;
import com.tigshop.bean.model.promotion.ShortUrl;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短链接服务实现类
 *
 * @author Tigshop团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlServiceImpl extends BaseServiceImpl<ShortUrlMapper, ShortUrl> implements ShortUrlService {

    private final WechatApiClient wechatApiClient;
    private final ConfigService configService;
    private final WechatOAuthService wechatOAuthService;

    @Override
    public String getLongUrlByCode(String code) {
        ShortUrl shortUrl = this.lambdaQuery().like(ShortUrl::getShortCode, code).last("limit 1").one();
        Assert.notNull(shortUrl, () -> new GlobalException("短链接暂时无法访问，请稍后再试..."));
        return shortUrl.getLongUrl();
    }

    @Override
    public ShortUrl getByCode(String code) {
        ShortUrl shortUrl = this.lambdaQuery().like(ShortUrl::getShortCode, code).last("limit 1").one();
        Assert.notNull(shortUrl, () -> new GlobalException("短链接暂时无法访问，请稍后再试..."));
        return shortUrl;
    }

    /**
     * 获取微信短链（使用小程序凭证）
     * @return String
     */
    @Override
    public String getWechatShortLink(String shareLink, Integer id) {
        return getWechatShortLink(shareLink, id, "商品详情");
    }

    @Override
    public String getWechatShortLink(String shareLink, Integer id, String pageTitle) {
        String pageUrl = shareLink + (id == null ? "" : id);
        String appId = configService.getConfigVal(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID);
        String appSecret = configService.getConfigVal(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET);
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            return pageUrl;
        }
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        if (tokenResult == null || StrUtil.isBlank(tokenResult.getAccessToken())) {
            return pageUrl;
        }
        while (pageUrl.startsWith("/")) {
            pageUrl = pageUrl.substring(1);
        }
        ShortLinkParam param = ShortLinkParam.builder()
                .pageTitle(StrUtil.blankToDefault(pageTitle, "推广链接"))
                .pageUrl(pageUrl)
                .build();
        ShortLinkResult result = wechatApiClient.genwxashortlink(tokenResult.getAccessToken(), param);
        if (result != null && result.getErrcode() == 0) {
            return result.getLink();
        }
        return shareLink + (id == null ? "" : id);
    }

    // 获取网页短链
    @Override
    public String getWebShortLink(String shareLink, Integer id, String shareType) {
        String baseUrl = shareLink + id;
        String encode = StrUtil.format("/s/{}", Base62.encode(shareType + id.toString()));
        long count = this.count(Wrappers.lambdaQuery(ShortUrl.class).eq(ShortUrl::getShortCode, encode));
        if (count > 0) {
            return encode;
        }
        ShortUrl shortUrl = ShortUrl.builder()
                .longUrl(baseUrl)
                .shortCode(encode)
                .addTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .build();
        this.save(shortUrl);
        return encode;
    }

    @Override
    @Transactional
    public String getWebShortLinkByAutoId(String shareLink, Integer id, String shareType) {
        String baseUrl = shareLink + (id == null ? "" : id);
        ShortUrl existing = this.lambdaQuery().eq(ShortUrl::getLongUrl, baseUrl).eq(ShortUrl::getType, 1).last("limit 1").one();
        if (existing != null && StrUtil.isNotBlank(existing.getShortCode())) return existing.getShortCode();
        ShortUrl record = ShortUrl.builder().longUrl(baseUrl).shortCode("p-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20)).type(1).addTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")).build();
        this.save(record);
        String code = StrUtil.format("/s/{}", Base62.encode(record.getShortUrlId().toString()));
        record.setShortCode(code);
        this.updateById(record);
        return code;
    }

    @Override
    public String getPcWebShortLink(String shareLink, String id, String shareType) {
        String baseUrl = shareLink + id;
        String encode = StrUtil.format("/s/{}", Base62.encode(shareType + id));
        long count = this.count(Wrappers.lambdaQuery(ShortUrl.class).eq(ShortUrl::getShortCode, encode));
        if (count > 0) {
            return encode;
        }
        ShortUrl shortUrl = ShortUrl.builder()
                .longUrl(baseUrl)
                .shortCode(encode)
                //pc
                .type(2)
                .addTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .build();
        this.save(shortUrl);
        return encode;
    }

    @Override
    public DecoratePromoteVO generateDecoratePromoteShortLink(String longUrl) {
        if (StrUtil.isBlank(longUrl)) {
            throw new GlobalException("链接不能为空");
        }
        String origin;
        String pathAndQuery;
        try {
            URI uri = URI.create(longUrl.trim());
            if (StrUtil.isBlank(uri.getScheme()) || StrUtil.isBlank(uri.getAuthority())) {
                throw new GlobalException("链接格式不正确");
            }
            origin = uri.getScheme() + "://" + uri.getAuthority();
            String rawPath = uri.getRawPath();
            String rawQuery = uri.getRawQuery();
            pathAndQuery = (StrUtil.isBlank(rawPath) ? "/" : rawPath)
                    + (StrUtil.isBlank(rawQuery) ? "" : "?" + rawQuery);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("链接格式不正确");
        }

        String h5ShortCode = findOrCreateShortCode(pathAndQuery, 1, shareTypeEnum.DECORATE.getKey());
        String pcShortCode = findOrCreateShortCode(pathAndQuery, 2, shareTypeEnum.DECORATE.getKey() + "pc");

        String h5ShortLink = origin + h5ShortCode;
        String pcShortLink = origin + pcShortCode;

        String wechatShortLink = null;
        String wechatShortLinkError = null;
        try {
            wechatShortLink = tryGetWechatShortLinkWithError(pathAndQuery, "装修推广");
        } catch (Exception e) {
            wechatShortLinkError = e.getMessage();
            log.warn("[generateDecoratePromoteShortLink] 微信短链生成失败：{}", e.getMessage());
        }

        String miniCode = "";
        try {
            String miniPath = convertToMiniProgramPath(pathAndQuery);
            if (StrUtil.isNotBlank(miniPath)) {
                miniCode = wechatOAuthService.getMiniCodeByFullPath(miniPath);
            }
        } catch (Exception e) {
            log.warn("[generateDecoratePromoteShortLink] 小程序码生成失败：{}", e.getMessage());
        }

        return DecoratePromoteVO.builder()
                .shortLink(h5ShortLink)
                .pcShortLink(pcShortLink)
                .wechatShortLink(wechatShortLink)
                .wechatShortLinkError(wechatShortLinkError)
                .miniCode(miniCode)
                .build();
    }

    private String findOrCreateShortCode(String pathAndQuery, int type, String seedPrefix) {
        String shortCode = this.lambdaQuery()
                .eq(ShortUrl::getLongUrl, pathAndQuery)
                .eq(ShortUrl::getType, type)
                .last("limit 1")
                .oneOpt()
                .map(ShortUrl::getShortCode)
                .orElse(null);
        if (StrUtil.isBlank(shortCode)) {
            String seedBase = seedPrefix + Integer.toString(pathAndQuery.hashCode());
            shortCode = StrUtil.format("/s/{}", Base62.encode(seedBase));
            int retry = 0;
            while (this.count(Wrappers.lambdaQuery(ShortUrl.class).eq(ShortUrl::getShortCode, shortCode)) > 0
                    && retry < 5) {
                retry++;
                shortCode = StrUtil.format("/s/{}", Base62.encode(seedBase + retry));
            }
            ShortUrl record = ShortUrl.builder()
                    .longUrl(pathAndQuery)
                    .shortCode(shortCode)
                    .type(type)
                    .addTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                    .build();
            this.save(record);
        }
        return shortCode;
    }

    private String tryGetWechatShortLinkWithError(String pageUrl, String pageTitle) {
        String appId = configService.getConfigVal(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID);
        String appSecret = configService.getConfigVal(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET);
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            throw new GlobalException("请先配置微信小程序AppID和AppSecret");
        }
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        if (tokenResult == null || StrUtil.isBlank(tokenResult.getAccessToken())) {
            throw new GlobalException("获取微信access_token失败");
        }

        String miniPath = convertToMiniProgramPath(pageUrl);
        if (StrUtil.isBlank(miniPath)) {
            miniPath = "pages/index/index";
        }

        ShortLinkParam param = ShortLinkParam.builder()
                .pageTitle(pageTitle)
                .pageUrl(miniPath)
                .build();
        ShortLinkResult result = wechatApiClient.genwxashortlink(tokenResult.getAccessToken(), param);
        if (result != null && result.getErrcode() == 0) {
            return result.getLink();
        }
        String errMsg = result != null ? StrUtil.format("微信短链生成失败[{}]：{}", result.getErrcode(), result.getErrmsg()) : "微信短链生成失败：响应为空";
        throw new GlobalException(errMsg);
    }

    private static String convertToMiniProgramPath(String pathAndQuery) {
        if (StrUtil.isBlank(pathAndQuery)) {
            return null;
        }
        String p = pathAndQuery.trim();
        if (p.startsWith("/mobile/")) {
            p = p.substring("/mobile/".length());
        } else if (p.startsWith("/mobile")) {
            p = p.substring("/mobile".length());
        }
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (StrUtil.isBlank(p) || "/".equals(p)) {
            return "pages/index/index";
        }
        return p;
    }
}
