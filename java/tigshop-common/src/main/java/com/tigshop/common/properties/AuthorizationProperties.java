package com.tigshop.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * 官网增强授权配置。
 *
 * <p>授权回源是尽力而为的外部能力，任何配置错误或官网不可达都不能阻止商城启动。
 * 私钥不放在数据库中，{@code identity-master-key-path} 指向本机受保护的密钥文件；
 * 生产环境可通过挂载 Secret 或外部密钥服务生成该文件。</p>
 */
@Data
@ConfigurationProperties(prefix = "tigshop.authorization")
public class AuthorizationProperties {

    /** 是否启用实例身份与回源。 */
    private boolean enabled = true;

    /** 官网公开回源接口的地址（官网无内部网关）。 */
    private String baseUrl = "https://api.tigshop.com";

    /**
     * 实例身份文件，默认放在当前运行用户的家目录，兼容 macOS/Linux 普通用户直接启动 JAR。
     * 生产环境可通过配置覆盖到专用持久化目录。
     */
    private String identityPath = defaultIdentityPath();

    /** 加密私钥使用的本机主密钥文件；为空时使用 identityPath.master.key。 */
    private String identityMasterKeyPath;

    /** 下载服务写入源码包的清单文件。 */
    private String packageManifestPath = "config/tigshop-package-distribution.json";

    private long connectTimeoutMs = 3000;
    private long readTimeoutMs = 5000;

    /** 当前阶段只核对和记录，不限制官方能力。 */
    private String enforcementMode = "OBSERVE_ONLY";

    /** 官网异常时放行本地/云服务调用。 */
    private String errorMode = "FAIL_OPEN";

    /** 是否由当前应用进程执行启动时 register/sync；API 开启，AdminAPI 关闭。 */
    private boolean startupReportEnabled;

    private static String defaultIdentityPath() {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            userHome = ".";
        }
        return Path.of(userHome, ".tigshop", "identity", "instance.json").toString();
    }
}
