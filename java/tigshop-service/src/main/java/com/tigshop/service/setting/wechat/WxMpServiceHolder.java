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
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 公众号 {@link WxMpService} 的统一获取入口。
 *
 * <p>背景：{@code WxMpConfig#wxService()} 是 Spring 单例 Bean，仅在首次创建时从数据库读取一次
 * appId/secret/token/aesKey 并缓存进 {@link WxMpConfigStorage}；后台修改公众号配置写库后，
 * 该单例不会感知，导致菜单发布、客服消息等链路继续使用旧凭证。
 *
 * <p>本类提供「变更检测式刷新」：每次 {@link #get()} 读取库中最新四项凭证，与单例当前 storage 比对，
 * 仅当发生变化时才调用 {@link WxMpService#setWxMpConfigStorage} 重建 storage。
 * 仅在凭证真正变化时重建，可避免无谓刷新丢失 {@code access_token} 缓存而触发微信频率限制。
 *
 * <p>未配置语义：四项凭证任一为空时返回 {@code null}，与既有消费方（探测降级、发布失败兜底）保持一致。
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
public class WxMpServiceHolder {

    @Resource
    private ConfigService configService;

    /**
     * 单例 {@link WxMpService}（由 {@code WxMpConfig#wxService()} 提供）。
     * {@code @Lazy} 延后解析，避免与配置类构建顺序耦合；仅在凭证齐全时才会被实际触发创建。
     */
    @Resource
    @Lazy
    private WxMpService wxMpService;

    /**
     * 获取已与库中最新凭证对齐的 {@link WxMpService}；公众号未配置时返回 {@code null}。
     */
    public WxMpService get() {
        String appId = readVal(SettingsEnum.WECHAT_APP_ID);
        String secret = readVal(SettingsEnum.WECHAT_APP_SECRET);
        String token = readVal(SettingsEnum.WECHAT_SERVER_TOKEN);
        String aesKey = readVal(SettingsEnum.WECHAT_SERVER_SECRET);
        if (StrUtil.hasBlank(appId, secret, token, aesKey)) {
            return null;
        }

        WxMpService service = wxMpService;
        WxMpConfigStorage cur = service.getWxMpConfigStorage();
        if (needRefresh(cur, appId, secret, token, aesKey)) {
            synchronized (this) {
                cur = service.getWxMpConfigStorage();
                if (needRefresh(cur, appId, secret, token, aesKey)) {
                    WxMpDefaultConfigImpl cfg = new WxMpDefaultConfigImpl();
                    cfg.setAppId(appId);
                    cfg.setSecret(secret);
                    cfg.setToken(token);
                    cfg.setAesKey(aesKey);
                    service.setWxMpConfigStorage(cfg);
                    log.info("[WxMp] 检测到公众号配置变更，已刷新 WxMpService storage，appId={}", appId);
                }
            }
        }
        return service;
    }

    private boolean needRefresh(WxMpConfigStorage cur, String appId, String secret, String token, String aesKey) {
        return cur == null
                || !appId.equals(cur.getAppId())
                || !secret.equals(cur.getSecret())
                || !token.equals(cur.getToken())
                || !aesKey.equals(cur.getAesKey());
    }

    private String readVal(SettingsEnum settingsEnum) {
        ConfigPO po = configService.getConfigByCode(settingsEnum.getBizCode());
        return po == null ? null : po.getBizVal();
    }
}
