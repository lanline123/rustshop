package com.tigshop.service.decorate.config;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.setting.config.LicensedSettingsVO;
import com.tigshop.common.properties.AuthorizationProperties;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.authorization.InstanceIdentityService;
import com.tigshop.service.authorization.RootDomainNormalizer;
import com.tigshop.service.setting.ConfigService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 官网装修模板库开放接口鉴权参数注入
 * <p>
 * 为出站请求补充官网要求的 licenseSn（授权码）、domain（访问域名）、version（系统版本号）。
 *
 * @author Tigshop团队
 */
@Component
@RequiredArgsConstructor
public class OpenTemplateRequestInterceptor implements RequestInterceptor {

    private final ConfigService configService;
    private final TigshopProperties tigshopProperties;
    private final AuthorizationProperties authorizationProperties;
    private final InstanceIdentityService identityService;

    @Override
    public void apply(RequestTemplate template) {
        String targetName = template.feignTarget().name();
        if (!"openTemplate".equals(targetName) && !"authorizationLifecycle".equals(targetName)) {
            return;
        }
        String domain = "";
        if ("openTemplate".equals(targetName)) {
            LicensedSettingsVO licensed = configService.getSettings(LicensedSettingsVO.class);
            if (licensed != null && licensed.getLicense() != null) {
                template.query("licenseSn", licensed.getLicense());
            }
            // 模板列表按前端访问站点筛选；不能使用实例授权配置中的 root-domain，
            // 因为同一后台可能代理多个站点域名。
            domain = resolveDomain();
            if (StrUtil.isNotBlank(domain)) {
                template.query("domain", domain);
            }
            template.query("version", stripVersionPrefix(tigshopProperties.getVersionNum()));
        }
        String resolvedRootDomain = RootDomainNormalizer.normalize(domain);
        if (resolvedRootDomain.isBlank()) {
            resolvedRootDomain = RootDomainNormalizer.normalize(identityService.current().rootDomain());
        }
        String signedRootDomain = resolvedRootDomain;
        identityService.sign(template.method(), template.url(), template.body())
                .ifPresent(signature -> addSignatureHeaders(template, signature, signedRootDomain));
    }

    /**
     * domain 以前端传入的访问域名为准；未传入时回退到当前请求 Host。
     */
    private String resolveDomain() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String domain = attributes.getRequest().getParameter("domain");
            if (StrUtil.isNotBlank(domain)) {
                return domain;
            }
        }
        String requestDomain = HeaderUtils.getRequestDomain();
        if (StrUtil.isNotBlank(requestDomain)) {
            return requestDomain;
        }
        String configuredDomain = configuredDomain(SettingsEnum.PC_DOMAIN);
        if (StrUtil.isNotBlank(configuredDomain)) {
            return configuredDomain;
        }
        configuredDomain = configuredDomain(SettingsEnum.H5_DOMAIN);
        if (StrUtil.isNotBlank(configuredDomain)) {
            return configuredDomain;
        }
        return configuredDomain(SettingsEnum.ADMIN_DOMAIN);
    }

    private String configuredDomain(SettingsEnum setting) {
        try {
            return configService.getConfigVal(setting);
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private void addSignatureHeaders(RequestTemplate template,
                                     InstanceIdentityService.SignatureHeaders signature,
                                     String rootDomain) {
        template.header("X-Tigshop-Signature-Version", "TIGSHOP-AUTH-V1");
        template.header("X-Tigshop-Installation-Id", signature.installationId());
        template.header("X-Tigshop-Package-Distribution-Id", signature.packageDistributionId());
        template.header("X-Tigshop-Key-Id", "sha256:" + signature.keyId());
        template.header("X-Tigshop-Timestamp", signature.timestamp());
        template.header("X-Tigshop-Nonce", signature.nonce());
        template.header("X-Tigshop-Request-Id", signature.requestId());
        template.header("X-Tigshop-Signature", signature.signature());
        template.header("X-Tigshop-Authorization-Mode", authorizationProperties.getEnforcementMode());
        String normalized = RootDomainNormalizer.normalize(rootDomain);
        if (!normalized.isBlank()) {
            template.header("X-Tigshop-Root-Domain", normalized);
        }
    }

    /**
     * 官网按语义化版本（如 5.8.32）比较，系统 versionNum 形如 v5.8.33，去掉前导 v 再传。
     */
    private String stripVersionPrefix(String version) {
        if (version == null) {
            return null;
        }
        return (version.startsWith("v") || version.startsWith("V")) ? version.substring(1) : version;
    }
}
