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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * Apple Pay 支付 Token 解密默认实现（纯 JDK JCA，支持 EC_v1）。
 *
 * <p>用支付处理证书私钥（{@link ApplePayCertLocator#PROCESSING_PRIVATE_KEY}）对 {@code token.paymentData} 解密：
 * EC_v1（Visa/MasterCard 等，ECDH + ConcatKDF + AES-256-GCM，处理私钥为 EC）；
 * RSA_v1（银联部分场景，RSA-OAEP 解对称密钥 + AES-256-GCM，处理私钥为 RSA）。</p>
 *
 * <p>安全（BR-07）：解密结果只返回给收单，禁止落库/日志。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultApplePayTokenDecryptor implements ApplePayTokenDecryptor {

    private final ConfigService configService;
    private final ApplePayCertLocator certLocator;
    private final MessageHelper messageHelper;

    @Override
    public String decrypt(String paymentTokenJson) {
        if (StrUtil.isBlank(paymentTokenJson)) {
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }
        if (!"1".equals(configService.getConfigVal(SettingsEnum.APPLE_PAY_PROCESSING_PRIVATE_KEY))) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }

        // 解析 token，兼容传入完整 token（含 paymentData）或直接传 paymentData
        JSONObject root = JSONObject.parseObject(paymentTokenJson);
        JSONObject paymentData = root.getJSONObject("paymentData");
        if (paymentData == null) {
            paymentData = root;
        }
        String version = paymentData.getString("version");
        String data = paymentData.getString("data");
        JSONObject header = paymentData.getJSONObject("header");
        if (StrUtil.isBlank(data) || header == null) {
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }

        File keyFile = certLocator.processingPrivateKeyFile();
        if (!keyFile.exists()) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }

        try {
            PrivateKey privateKey = ApplePayCryptoUtils.loadPrivateKey(keyFile);
            byte[] decrypted;
            if (version != null && version.startsWith("RSA")) {
                // RSA_v1（银联部分场景）：header.wrappedKey + RSA 处理私钥
                String wrappedKey = header.getString("wrappedKey");
                if (StrUtil.isBlank(wrappedKey)) {
                    throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
                }
                decrypted = ApplePayCryptoUtils.decryptRsaV1(privateKey, wrappedKey, data);
            } else {
                // EC_v1（默认）：header.ephemeralPublicKey + EC 处理私钥 + SHA-256(merchantId)
                String ephemeralPublicKey = header.getString("ephemeralPublicKey");
                if (StrUtil.isBlank(ephemeralPublicKey)) {
                    throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
                }
                String merchantId = configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_ID);
                decrypted = ApplePayCryptoUtils.decryptEcV1(privateKey, ephemeralPublicKey, merchantId, data);
            }
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            // 不打印明文/密钥，仅记录异常类型
            log.error("Apple Pay Token 解密失败：{}", e.getClass().getSimpleName());
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }
    }
}
