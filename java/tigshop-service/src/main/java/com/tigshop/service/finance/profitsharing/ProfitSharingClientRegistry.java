package com.tigshop.service.finance.profitsharing;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.exception.GlobalException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 分账渠道客户端注册表。
 *
 * <p>按 {@link ProfitSharingApiClient#channel()} 自注册，不依赖 Bean 名或 switch 分支；
 * 汇付分账模块被 VORT 剥离时，对应 Bean 不存在且不会影响微信分账启动。</p>
 */
@Component
public class ProfitSharingClientRegistry {

    private final Map<String, ProfitSharingApiClient> byChannel;

    public ProfitSharingClientRegistry(List<ProfitSharingApiClient> clients) {
        Map<String, ProfitSharingApiClient> map = new LinkedHashMap<>();
        for (ProfitSharingApiClient client : clients) {
            String channel = normalize(client.channel());
            if (map.containsKey(channel)) {
                throw new IllegalStateException("重复的分账渠道实现: " + channel);
            }
            map.put(channel, client);
        }
        this.byChannel = Map.copyOf(map);
    }

    public ProfitSharingApiClient get(String payChannel) {
        String channel = normalize(payChannel);
        ProfitSharingApiClient client = byChannel.get(channel);
        if (client == null) {
            throw new GlobalException("分账渠道未启用或不存在: " + channel);
        }
        return client;
    }

    private static String normalize(String channel) {
        if (StrUtil.isBlank(channel)) {
            return "";
        }
        return channel.trim().toUpperCase(Locale.ROOT);
    }
}
