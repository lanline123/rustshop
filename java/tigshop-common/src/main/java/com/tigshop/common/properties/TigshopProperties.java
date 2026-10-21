package com.tigshop.common.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Tigshop 版本
 *
 * @author kidd
 * @since 2025/7/3 11:13
 */
@ConfigurationProperties(prefix = "tigshop")
@AllArgsConstructor
@Getter
public class TigshopProperties {

    /** 系统名称 */
    private final String name;

    /** 系统版本名 */
    private final String version;

    /** 系统版本号 */
    private final String versionNum;

    /** 是否商户端（1 是，0 否） */
    private final Integer isMerchant;

    /** 是否启用 B2B 模式（1 是，0 否） */
    private final Integer isB2b;

    /** 是否为 Pro 版本（1 是，0 否） */
    private final Integer isPro;

    /** 是否海外版（1 是，0 否） */
    private final Integer isOverseas;

    /** 是否供应商端（1 是，0 否） */
    private final Integer isVendor;

    /** 是否多门店（1 是，0 否） */
    private final Integer isO2o;

    /** 是否启用批发功能菜单（兼容旧配置，菜单显隐由 VORT common.wholesale 决定） */
    private final Integer isCommonWholesale;

    /** 是否演示模式-开启手机号脱敏（1 是，0 否），默认 0 */
    private final Integer isDemo;

    /** 是否调试模式（1 是-本地/演示站，0 否-发客户），默认 0。统一控制本地/演示站的错误详情暴露、详细日志等调试行为 */
    private final Integer isDebug;

    /** 是否处于调试模式（本地/演示站） */
    public boolean debugEnabled() {
        return isDebug != null && isDebug == 1;
    }
}
