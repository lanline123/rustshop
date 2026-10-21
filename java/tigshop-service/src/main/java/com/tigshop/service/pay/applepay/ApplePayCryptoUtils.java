package com.tigshop.service.pay.applepay;

import cn.hutool.core.io.FileUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Apple Pay 密码学工具（纯 JDK JCA 实现，不依赖 BouncyCastle）。
 *
 * <p>支持 EC_v1 支付 Token 解密（ECDH + NIST SP800-56A 单步 KDF + AES-256-GCM）与 PEM 解析。
 * 私钥须为 PKCS#8 格式（-----BEGIN PRIVATE KEY-----）；SEC1 格式请先 openssl pkcs8 转换。</p>
 *
 * @author Tigshop团队
 */
public final class ApplePayCryptoUtils {

    private ApplePayCryptoUtils() {
    }

    /**
     * 读取 PEM 文件中的 EC 私钥（PKCS#8）。
     */
    public static PrivateKey loadEcPrivateKey(File pemFile) throws Exception {
        String pem = FileUtil.readUtf8String(pemFile);
        byte[] der = decodePemBody(pem);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    /**
     * 读取 PEM 文件中的私钥（PKCS#8），自动尝试 RSA 与 EC（商户身份证书私钥类型不固定）。
     */
    public static PrivateKey loadPrivateKey(File pemFile) throws Exception {
        String pem = FileUtil.readUtf8String(pemFile);
        byte[] der = decodePemBody(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        for (String algorithm : new String[]{"RSA", "EC"}) {
            try {
                return KeyFactory.getInstance(algorithm).generatePrivate(spec);
            } catch (Exception ignore) {
                // 尝试下一种算法
            }
        }
        throw new IllegalArgumentException("无法解析私钥（非 RSA/EC PKCS#8）");
    }

    /**
     * 读取 PEM 文件中的 X.509 证书。
     */
    public static X509Certificate loadCertificate(File pemFile) throws Exception {
        byte[] content = FileUtil.readBytes(pemFile);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(content));
    }

    /**
     * 由 Base64(DER SubjectPublicKeyInfo) 解析 EC 公钥（Apple 面板返回的 ephemeralPublicKey）。
     */
    public static PublicKey parseEcPublicKey(String base64Der) throws Exception {
        byte[] der = Base64.getDecoder().decode(base64Der);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(new X509EncodedKeySpec(der));
    }

    /**
     * EC_v1 解密。
     *
     * @param processingPrivateKey 支付处理证书私钥（EC）
     * @param ephemeralPublicKey   Apple token header.ephemeralPublicKey（Base64 DER）
     * @param merchantId           商户标识（merchant.com.xxx），用于派生 KDF PartyVInfo
     * @param encryptedDataBase64  token.data（Base64，AES-256-GCM 密文，末尾 16 字节为 tag）
     * @return 解密后的明文字节（卡凭据，仅内存使用，禁落库/日志）
     */
    public static byte[] decryptEcV1(PrivateKey processingPrivateKey, String ephemeralPublicKey,
                                     String merchantId, String encryptedDataBase64) throws Exception {
        PublicKey ephemeral = parseEcPublicKey(ephemeralPublicKey);

        // 1. ECDH 共享密钥 Z
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(processingPrivateKey);
        ka.doPhase(ephemeral, true);
        byte[] sharedSecret = ka.generateSecret();

        // 2. NIST SP800-56A 单步 KDF（keydatalen=256=hashlen，单次 SHA-256 即可）
        byte[] symmetricKey = deriveSymmetricKey(sharedSecret, merchantId);

        // 3. AES-256-GCM 解密（Apple 使用 16 字节全零 IV，tag 128 位）
        byte[] cipherWithTag = Base64.getDecoder().decode(encryptedDataBase64);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[16];
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(symmetricKey, "AES"),
                new GCMParameterSpec(128, iv));
        return cipher.doFinal(cipherWithTag);
    }

    /**
     * RSA_v1 解密（银联部分场景）。
     *
     * <p>流程：用支付处理证书 RSA 私钥以 RSA-OAEP(SHA-256, MGF1-SHA-256) 解出 AES-256 对称密钥，
     * 再用该密钥 AES-256-GCM（16 字节全零 IV，tag 128 位）解密 data。</p>
     *
     * @param processingPrivateKey 支付处理证书私钥（RSA）
     * @param wrappedKeyBase64     token header.wrappedKey（Base64，RSA 加密的对称密钥）
     * @param encryptedDataBase64  token.data（Base64，AES-256-GCM 密文）
     * @return 解密后的明文字节（卡凭据，仅内存使用，禁落库/日志）
     */
    public static byte[] decryptRsaV1(PrivateKey processingPrivateKey, String wrappedKeyBase64,
                                      String encryptedDataBase64) throws Exception {
        // 1. RSA-OAEP(SHA-256/MGF1-SHA-256) 解出 AES 对称密钥
        byte[] wrapped = Base64.getDecoder().decode(wrappedKeyBase64);
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaep = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsa.init(Cipher.DECRYPT_MODE, processingPrivateKey, oaep);
        byte[] symmetricKey = rsa.doFinal(wrapped);

        // 2. AES-256-GCM 解密 data（16 字节全零 IV，tag 128 位）
        byte[] cipherWithTag = Base64.getDecoder().decode(encryptedDataBase64);
        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(symmetricKey, "AES"),
                new GCMParameterSpec(128, new byte[16]));
        return aes.doFinal(cipherWithTag);
    }

    /**
     * 派生对称密钥：SHA-256( 0x00000001 || Z || AlgorithmID || PartyUInfo || PartyVInfo )。
     * AlgorithmID = 0x0D || "id-aes256-GCM"；PartyUInfo = "Apple"；PartyVInfo = SHA-256(merchantId)。
     */
    private static byte[] deriveSymmetricKey(byte[] sharedSecret, String merchantId) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] merchantIdHash = sha256.digest(merchantId.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream kdfInput = new ByteArrayOutputStream();
        // counter = 1
        kdfInput.write(new byte[]{0x00, 0x00, 0x00, 0x01});
        // Z
        kdfInput.write(sharedSecret);
        // AlgorithmID
        byte[] algId = "id-aes256-GCM".getBytes(StandardCharsets.US_ASCII);
        kdfInput.write(algId.length);
        kdfInput.write(algId);
        // PartyUInfo
        kdfInput.write("Apple".getBytes(StandardCharsets.US_ASCII));
        // PartyVInfo
        kdfInput.write(merchantIdHash);

        MessageDigest kdf = MessageDigest.getInstance("SHA-256");
        return kdf.digest(kdfInput.toByteArray());
    }

    /**
     * 去掉 PEM 头尾与换行，Base64 解码为 DER。
     */
    private static byte[] decodePemBody(String pem) {
        String body = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }
}
