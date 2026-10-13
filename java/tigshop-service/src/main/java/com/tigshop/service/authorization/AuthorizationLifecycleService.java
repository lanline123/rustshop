package com.tigshop.service.authorization;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.setting.config.LicensedSettingsVO;
import com.tigshop.common.properties.AuthorizationProperties;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.feign.AuthorizationLifecycleApiClient;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** API 实例启动、授权变化和版本变化时向官网回源；不包含定时任务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationLifecycleService implements ApplicationRunner {

    private final AuthorizationProperties properties;
    private final TigshopProperties tigshopProperties;
    private final ConfigService configService;
    private final InstanceIdentityService identityService;
    private final AuthorizationLifecycleApiClient lifecycleApiClient;

    @Override
    public void run(ApplicationArguments args) {
        onStartup();
    }

    public void onStartup() {
        if (!properties.isEnabled() || !properties.isStartupReportEnabled()) {
            return;
        }
        try {
            InstanceIdentityService.IdentitySnapshot identity = identityService.initialize();
            if (!identity.available()) {
                return;
            }
            String rootDomain = resolveRootDomain(identity);
            boolean domainChanged = !rootDomain.isBlank()
                    && !rootDomain.equalsIgnoreCase(identity.rootDomain());
            String trigger = !identity.previousPackageDistributionId().isBlank()
                    ? "VERSION_UPGRADED"
                    : domainChanged
                    ? "DOMAIN_CHANGED"
                    : sameVersion(identity.lastSyncedVersion(), tigshopProperties.getVersionNum())
                    ? "SYSTEM_START" : "VERSION_UPGRADED";
            if (!identity.registered()) {
                call("REGISTER", identity, true, rootDomain);
            } else {
                call(trigger, identity, false, rootDomain);
            }
        } catch (Exception ex) {
            // 回源是外部副作用，失败只记录，绝不阻止应用启动。
            log.warn("Tigshop 官网授权回源失败，按 {} 继续启动", properties.getErrorMode(), ex);
        }
    }

    public void onAuthorizationChanged() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            InstanceIdentityService.IdentitySnapshot identity = identityService.current();
            if (identity.available()) {
                call("AUTHORIZATION_CHANGED", identity, false, resolveRootDomain(identity));
            }
        } catch (Exception ex) {
            log.warn("授权变化回源失败，保留本地授权配置并继续运行", ex);
        }
    }

    private void call(String trigger, InstanceIdentityService.IdentitySnapshot identity, boolean register,
                      String rootDomain) {
        Map<String, Object> request = requestBody(trigger, identity, rootDomain);
        Map<String, Object> response = register
                ? lifecycleApiClient.register(request) : lifecycleApiClient.sync(request);
        if (isBusinessError(response)) {
            throw new IllegalStateException("官网返回授权核对失败: " + response.get("message"));
        }
        identityService.markSynced(tigshopProperties.getVersionNum(), rootDomain);
    }

    private Map<String, Object> requestBody(String trigger, InstanceIdentityService.IdentitySnapshot identity,
                                            String rootDomain) {
        LicensedSettingsVO licensed = configService.getSettings(LicensedSettingsVO.class);
        Map<String, Object> environment = new LinkedHashMap<>();
        environment.put("os", System.getProperty("os.name", ""));
        environment.put("osVersion", System.getProperty("os.version", ""));
        environment.put("arch", System.getProperty("os.arch", ""));
        environment.put("javaVersion", System.getProperty("java.version", ""));
        environment.put("deployment", System.getProperty("tigshop.deployment", "unknown"));
        Map<String, Object> publicKey = new LinkedHashMap<>();
        publicKey.put("algorithm", "Ed25519");
        publicKey.put("value", identity.publicKey());
        publicKey.put("fingerprint", "sha256:" + identity.keyFingerprint());
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("packageDistributionId", identity.packageDistributionId());
        request.put("previousPackageDistributionId", identity.previousPackageDistributionId());
        request.put("installationId", identity.installationId());
        request.put("packageDistributionProof", identity.packageDistributionProof());
        request.put("publicKey", publicKey);
        request.put("authorizationCode", licensed == null ? "" : safe(licensed.getLicense()));
        request.put("version", stripVersionPrefix(tigshopProperties.getVersionNum()));
        request.put("rootDomain", rootDomain == null ? "" : rootDomain);
        request.put("runtime", environment);
        request.put("trigger", trigger);
        request.put("hostname", hostname());
        return request;
    }

    /**
     * 根域名不再读取 YAML 中的 TIGSHOP_ROOT_DOMAIN。
     * 优先使用当前请求 Host；启动/后台触发时回退到商城自身保存的 PC/H5 域名，
     * 最后才沿用身份文件中上一次成功上报的值。
     */
    private String resolveRootDomain(InstanceIdentityService.IdentitySnapshot identity) {
        String rootDomain = RootDomainNormalizer.normalize(HeaderUtils.getRequestDomain());
        if (!rootDomain.isBlank()) {
            return rootDomain;
        }
        rootDomain = configuredRootDomain(SettingsEnum.PC_DOMAIN);
        if (!rootDomain.isBlank()) {
            return rootDomain;
        }
        rootDomain = configuredRootDomain(SettingsEnum.H5_DOMAIN);
        if (!rootDomain.isBlank()) {
            return rootDomain;
        }
        rootDomain = configuredRootDomain(SettingsEnum.ADMIN_DOMAIN);
        if (!rootDomain.isBlank()) {
            return rootDomain;
        }
        return RootDomainNormalizer.normalize(identity.rootDomain());
    }

    private String configuredRootDomain(SettingsEnum setting) {
        try {
            return RootDomainNormalizer.normalize(configService.getConfigVal(setting));
        } catch (RuntimeException ex) {
            log.debug("读取商城域名配置失败，跳过该来源: {}", setting, ex);
            return "";
        }
    }

    private boolean isBusinessError(Map<String, Object> response) {
        if (response == null) {
            return true;
        }
        Object errcode = response.get("errcode");
        if (errcode == null && response.get("data") instanceof Map<?, ?> data) {
            errcode = data.get("errcode");
        }
        if (errcode instanceof Number number) {
            return number.intValue() > 0;
        }
        return false;
    }

    private static boolean sameVersion(String previous, String current) {
        return previous != null && current != null && previous.equalsIgnoreCase(current);
    }

    private static String stripVersionPrefix(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).startsWith("v") ? value.substring(1) : value;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "";
        }
    }
}
