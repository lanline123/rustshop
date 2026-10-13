// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 地图服务商，对应配置项 {@link SettingsEnum#MAP_PROVIDER}。
 *
 * @author Tigshop团队
 * @create 2026/7/31
 */
@Getter
@AllArgsConstructor
public enum MapProviderEnum {

    /** 高德地图，坐标系 GCJ-02 */
    AMAP("amap"),

    /** 天地图，坐标系 CGCS2000 */
    TIANDITU("tianditu");

    private final String code;

    /**
     * 按配置值解析服务商。
     * 解析不出时返回 {@code null}，由调用方（MapProviderRouter）决定回落策略，
     * 本方法不自行兜底，避免把「配置异常」静默成「配置正确」。
     */
    public static MapProviderEnum of(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String target = code.trim();
        for (MapProviderEnum item : values()) {
            if (item.code.equalsIgnoreCase(target)) {
                return item;
            }
        }
        return null;
    }
}
