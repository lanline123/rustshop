// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.map;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.MapProviderEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.service.setting.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 按 {@link SettingsEnum#MAP_PROVIDER} 配置选择当前生效的地图服务商实现。
 * <p>
 * 回落规则：配置为空、空白或未知取值时一律回落高德，保证存量部署在未落库该配置时行为不变。
 * 回落只发生在「选实现」这一步，<b>不是</b>调用失败后的跨服务商兜底。
 *
 * @author Tigshop团队
 * @create 2026/7/31
 */
@Component
@Slf4j
public class MapProviderRouter {

    private static final MapProviderEnum FALLBACK = MapProviderEnum.AMAP;

    private final ConfigService configService;

    private final Map<MapProviderEnum, MapProvider> providers;

    public MapProviderRouter(ConfigService configService, List<MapProvider> providers) {
        this.configService = configService;
        Map<MapProviderEnum, MapProvider> indexed = new EnumMap<>(MapProviderEnum.class);
        for (MapProvider provider : providers) {
            indexed.put(provider.provider(), provider);
        }
        this.providers = indexed;
    }

    /**
     * 取当前生效的服务商实现。
     *
     * @throws IllegalStateException 连兜底的高德实现都不存在，属于装配错误
     */
    public MapProvider current() {
        MapProviderEnum target = resolveConfigured();
        MapProvider provider = providers.get(target);
        if (provider == null && target != FALLBACK) {
            log.warn("地图服务商 {} 无可用实现，回落 {}", target.getCode(), FALLBACK.getCode());
            provider = providers.get(FALLBACK);
        }
        if (provider == null) {
            throw new IllegalStateException("无可用地图服务商实现，target=" + target.getCode());
        }
        return provider;
    }

    /**
     * 取当前实际生效的服务商标识，供配置下发与日志使用。
     * 口径与 {@link #current()} 完全一致，避免「下发的服务商」与「真正调用的服务商」不一致。
     */
    public MapProviderEnum currentProvider() {
        return current().provider();
    }

    private MapProviderEnum resolveConfigured() {
        String configured = configService.getConfigVal(SettingsEnum.MAP_PROVIDER);
        MapProviderEnum target = MapProviderEnum.of(configured);
        if (target != null) {
            return target;
        }
        // 空值是「配置项尚未落库」的正常表现；只有明确写错才值得告警
        if (StrUtil.isNotBlank(configured)) {
            log.warn("地图服务商配置值非法，回落 {}：configured={}", FALLBACK.getCode(), configured);
        }
        return FALLBACK;
    }
}
