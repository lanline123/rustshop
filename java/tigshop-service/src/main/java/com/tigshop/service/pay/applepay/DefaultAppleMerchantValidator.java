package com.tigshop.service.pay.applepay;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * Apple Pay 商户校验默认实现（商户身份证书 + 双向 TLS 请求 Apple 会话）。
 *
 * <p>流程：SSRF 校验 validationUrl（*.apple.com）→ 用商户身份证书({@link ApplePayCertLocator#MERCHANT_IDENTITY_CERT})
 * 与私钥({@link ApplePayCertLocator#MERCHANT_IDENTITY_KEY})构建客户端证书 SSLContext → POST validationUrl
 * 携带 {merchantIdentifier, displayName, initiative:"web", initiativeContext} → 返回 merchantSession 原样透传。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAppleMerchantValidator implements AppleMerchantValidator {

    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    private final ConfigService configService;
    private final ApplePayCertLocator certLocator;
    private final MessageHelper messageHelper;

    @Override
    public String validateMerchant(String validationUrl, String displayDomain) {
        // 防 SSRF：validationUrl 必须为 apple.com 域（BR-06）
        if (!isAppleDomain(validationUrl)) {
            log.warn("Apple Pay 商户校验被拒绝，非法 validationUrl: {}", validationUrl);
            throw new GlobalException(messageHelper.get("error.applepay.merchant.validate.fail"));
        }

        // 证书配置完整性校验
        if (!"1".equals(configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_IDENTITY_CERT))
                || !"1".equals(configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_IDENTITY_KEY))) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }
        File certFile = certLocator.merchantIdentityCertFile();
        File keyFile = certLocator.merchantIdentityKeyFile();
        if (!certFile.exists() || !keyFile.exists()) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }

        String merchantId = configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_ID);
        String displayName = configService.getConfigVal(SettingsEnum.APPLE_PAY_DISPLAY_NAME);

        JSONObject body = new JSONObject();
        body.put("merchantIdentifier", merchantId);
        body.put("displayName", displayName);
        body.put("initiative", "web");
        body.put("initiativeContext", displayDomain);

        try {
            SSLContext sslContext = buildClientCertSslContext(certFile, keyFile);
            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validationUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || StrUtil.isBlank(response.body())) {
                log.warn("Apple Pay 商户校验返回异常，status={}", response.statusCode());
                throw new GlobalException(messageHelper.get("error.applepay.merchant.validate.fail"));
            }
            // merchantSession 原样透传
            return response.body();
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple Pay 商户校验异常：{}", e.getClass().getSimpleName());
            throw new GlobalException(messageHelper.get("error.applepay.merchant.validate.fail"));
        }
    }

    /**
     * 用商户身份证书 + 私钥构建仅含客户端证书的 SSLContext（用于与 Apple 建双向 TLS）。
     */
    private SSLContext buildClientCertSslContext(File certFile, File keyFile) throws Exception {
        X509Certificate cert = ApplePayCryptoUtils.loadCertificate(certFile);
        PrivateKey privateKey = ApplePayCryptoUtils.loadPrivateKey(keyFile);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("apple-merchant-identity", privateKey, KEYSTORE_PASSWORD,
                new java.security.cert.Certificate[]{cert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }

    /**
     * 校验 URL 主机是否属于 apple.com 域。
     */
    private boolean isAppleDomain(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            String host = uri.getHost();
            if (StrUtil.isBlank(host)) {
                return false;
            }
            host = host.toLowerCase();
            return host.equals("apple.com") || host.endsWith(".apple.com");
        } catch (Exception e) {
            return false;
        }
    }
}
