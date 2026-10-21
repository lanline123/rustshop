package com.tigshop.service.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.common.properties.AuthorizationProperties;
import com.tigshop.common.properties.TigshopProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 管理一套部署的 installationId 和 Ed25519 密钥。
 *
 * <p>身份文件和初始化标记均采用原子写入。初始化标记存在而身份文件缺失时进入
 * IDENTITY_LOST，绝不静默生成新身份；所有异常只更新状态并由上层按 FAIL_OPEN 继续运行。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstanceIdentityService {

    private static final String ALGORITHM = "Ed25519";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int MASTER_KEY_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthorizationProperties properties;
    private final TigshopProperties tigshopProperties;
    private final ObjectMapper objectMapper;
    private final Object lock = new Object();
    private volatile IdentitySnapshot snapshot = IdentitySnapshot.unavailable("NOT_INITIALIZED");
    private volatile boolean loaded;

    /** 应用启动和授权变化时调用；重复调用只读取既有身份。 */
    public IdentitySnapshot initialize() {
        synchronized (lock) {
            if (loaded) {
                return snapshot;
            }
            if (!properties.isEnabled()) {
                snapshot = IdentitySnapshot.unavailable("DISABLED");
                loaded = true;
                return snapshot;
            }
            Path identityPath = path(properties.getIdentityPath());
            Path markerPath = markerPath(identityPath);
            try {
                if (Files.exists(identityPath)) {
                    IdentitySnapshot loadedIdentity = read(identityPath);
                    snapshot = reconcilePackageManifest(identityPath, loadedIdentity);
                    loaded = true;
                    return snapshot;
                }
                if (Files.exists(markerPath)) {
                    snapshot = IdentitySnapshot.unavailable("IDENTITY_LOST");
                    loaded = true;
                    log.error("Tigshop 实例身份文件丢失，拒绝自动生成新 installationId: {}", identityPath);
                    return snapshot;
                }
                PackageManifest packageManifest = readPackageManifest();
                if (packageManifest.packageDistributionId().isBlank()) {
                    snapshot = IdentitySnapshot.unavailable("PACKAGE_MANIFEST_MISSING");
                    loaded = true;
                    log.warn("未找到 packageDistributionId 清单，跳过实例身份初始化: {}",
                            properties.getPackageManifestPath());
                    return snapshot;
                }
                KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
                KeyPair keyPair = generator.generateKeyPair();
                String masterKey = loadMasterKey(identityPath, true);
                String encryptedPrivateKey = encrypt(keyPair.getPrivate().getEncoded(), decodeKey(masterKey));
                IdentitySnapshot created = new IdentitySnapshot(
                        UUID.randomUUID().toString(), packageManifest.packageDistributionId(),
                        "",
                        packageManifest.packageDistributionProof(),
                        // 根域名来自运行时站点请求/商城域名配置，不依赖 YAML 的固定值。
                        "",
                        Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded()),
                        encryptedPrivateKey,
                        fingerprint(keyPair.getPublic()), false, "INITIALIZED",
                        tigshopProperties.getVersionNum(), Instant.now().toEpochMilli(), keyPair.getPrivate());
                // 先落初始化标记，再落身份文件，崩溃恢复时宁可人工重绑也不自动换身份。
                atomicWrite(markerPath, "initialized\n".getBytes(StandardCharsets.UTF_8));
                write(identityPath, created);
                snapshot = created;
                loaded = true;
                return snapshot;
            } catch (Exception ex) {
                snapshot = IdentitySnapshot.unavailable("IDENTITY_INIT_FAILED");
                loaded = true;
                log.error("Tigshop 实例身份初始化失败，继续启动并保持本地功能可用", ex);
                return snapshot;
            }
        }
    }

    public IdentitySnapshot current() {
        if (snapshot.available()) {
            return snapshot;
        }
        return initialize();
    }

    public Optional<SignatureHeaders> sign(String method, String pathAndQuery, byte[] body) {
        IdentitySnapshot identity = current();
        if (!identity.available() || identity.privateKey() == null) {
            return Optional.empty();
        }
        try {
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String nonce = randomToken(18);
            String requestId = UUID.randomUUID().toString();
            String bodyHash = sha256Hex(body == null ? new byte[0] : body);
            String canonical = "TIGSHOP-AUTH-V1\n" + method.toUpperCase() + "\n"
                    + pathAndQuery + "\n" + timestamp + "\n" + nonce + "\n"
                    + requestId + "\n" + bodyHash;
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(identity.privateKey());
            signature.update(canonical.getBytes(StandardCharsets.UTF_8));
            return Optional.of(new SignatureHeaders(identity.installationId(),
                    identity.packageDistributionId(), identity.keyFingerprint(), timestamp,
                    nonce, requestId, Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign()),
                    canonical));
        } catch (GeneralSecurityException ex) {
            log.warn("实例请求签名失败，按 FAIL_OPEN 放行本次请求", ex);
            return Optional.empty();
        }
    }

    public void markSynced(String version) {
        markSynced(version, null);
    }

    /**
     * 官网同步成功后记录本次实际上报的根域名，失败时不提前覆盖旧值，
     * 这样域名变化能够在下一次启动时继续触发 DOMAIN_CHANGED 同步。
     */
    public void markSynced(String version, String rootDomain) {
        synchronized (lock) {
            IdentitySnapshot current = current();
            if (!current.available()) {
                return;
            }
            String normalizedRootDomain = RootDomainNormalizer.normalize(rootDomain);
            if (normalizedRootDomain.isBlank()) {
                normalizedRootDomain = current.rootDomain();
            }
            IdentitySnapshot updated = current.withSync(version).withRootDomain(normalizedRootDomain);
            try {
                write(path(properties.getIdentityPath()), updated);
                snapshot = updated;
            } catch (Exception ex) {
                log.warn("实例同步状态写入失败，不影响本地功能", ex);
            }
        }
    }

    private IdentitySnapshot reconcilePackageManifest(Path identityPath, IdentitySnapshot identity) {
        PackageManifest manifest = readPackageManifest();
        if (manifest.packageDistributionId().isBlank()
                || (manifest.packageDistributionId().equals(identity.packageDistributionId())
                && manifest.packageDistributionProof().equals(identity.packageDistributionProof()))) {
            return identity;
        }
        IdentitySnapshot updated = identity.withPackage(manifest.packageDistributionId(),
                manifest.packageDistributionProof());
        try {
            write(identityPath, updated);
            return updated;
        } catch (Exception ex) {
            log.warn("检测到新的 packageDistributionId，但身份文件更新失败；下次启动继续重试", ex);
            return identity;
        }
    }

    private IdentitySnapshot read(Path path) throws IOException, GeneralSecurityException {
        JsonNode node = objectMapper.readTree(Files.readString(path));
        byte[] publicBytes = decodePublicKey(node.path("publicKey").asText());
        PublicKey publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(publicBytes));
        byte[] master = decodeKey(loadMasterKey(path, false));
        byte[] privateBytes = decrypt(node.path("encryptedPrivateKey").asText(), master);
        PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        return new IdentitySnapshot(node.path("installationId").asText(),
                node.path("packageDistributionId").asText(),
                node.path("previousPackageDistributionId").asText(),
                node.path("packageDistributionProof").asText(), node.path("rootDomain").asText(),
                node.path("publicKey").asText(), node.path("encryptedPrivateKey").asText(),
                node.path("keyFingerprint").asText(fingerprint(publicKey)),
                node.path("registered").asBoolean(false), node.path("status").asText("INITIALIZED"),
                node.path("lastSyncedVersion").asText(), node.path("createdAt").asLong(), privateKey);
    }

    private void write(Path path, IdentitySnapshot identity) throws IOException {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("installationId", identity.installationId());
        value.put("packageDistributionId", identity.packageDistributionId());
        value.put("previousPackageDistributionId", identity.previousPackageDistributionId());
        value.put("packageDistributionProof", identity.packageDistributionProof());
        value.put("rootDomain", identity.rootDomain());
        value.put("publicKey", identity.publicKey());
        value.put("encryptedPrivateKey", identity.encryptedPrivateKey());
        value.put("keyFingerprint", identity.keyFingerprint());
        value.put("registered", identity.registered());
        value.put("status", identity.status());
        value.put("lastSyncedVersion", identity.lastSyncedVersion());
        value.put("createdAt", identity.createdAt());
        atomicWrite(path, objectMapper.writeValueAsBytes(value));
    }

    private PackageManifest readPackageManifest() {
        try {
            Path configured = path(properties.getPackageManifestPath());
            Path manifest = resolvePackageManifest(configured);
            if (Files.exists(manifest)) {
                try (InputStream source = Files.newInputStream(manifest)) {
                    return parsePackageManifest(source);
                }
            }
            if (configured.isAbsolute()) {
                return new PackageManifest("", "");
            }
            String resourcePath = configured.toString().replace('\\', '/');
            while (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            try (InputStream resource = InstanceIdentityService.class.getClassLoader()
                    .getResourceAsStream(resourcePath)) {
                if (resource == null) {
                    return new PackageManifest("", "");
                }
                return parsePackageManifest(resource);
            }
        } catch (Exception ex) {
            log.warn("读取 packageDistributionId 清单失败，跳过实例初始化", ex);
            return new PackageManifest("", "");
        }
    }

    private PackageManifest parsePackageManifest(InputStream source) throws IOException {
        JsonNode node = objectMapper.readTree(source);
        String id = node.path("packageDistributionId").asText();
        String proof = node.path("packageDistributionProof").asText("");
        return new PackageManifest(id == null ? "" : id.trim(), proof);
    }

    /**
     * 相对清单路径优先按当前工作目录读取，再按 JAR/类路径位置向上查找，
     * 避免直接从任意工作目录启动 JAR 时丢失安装包清单。
     */
    private static Path resolvePackageManifest(Path configured) {
        if (Files.exists(configured) || configured.isAbsolute()) {
            return configured;
        }
        Path base = applicationLocation();
        for (int i = 0; base != null && i < 6; i++, base = base.getParent()) {
            Path candidate = base.resolve(configured).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return configured;
    }

    private static Path applicationLocation() {
        try {
            URI locationUri = InstanceIdentityService.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI();
            Path location = Path.of(locationUri);
            return Files.isRegularFile(location) ? location.getParent() : location;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String loadMasterKey(Path identityPath, boolean createIfMissing) throws IOException {
        Path configured = properties.getIdentityMasterKeyPath() == null
                || properties.getIdentityMasterKeyPath().isBlank()
                ? Path.of(identityPath + ".master.key") : path(properties.getIdentityMasterKeyPath());
        if (Files.exists(configured)) {
            return Files.readString(configured).trim();
        }
        if (!createIfMissing) {
            throw new IOException("identity master key file is missing: " + configured);
        }
        Files.createDirectories(configured.toAbsolutePath().getParent());
        byte[] bytes = new byte[MASTER_KEY_BYTES];
        RANDOM.nextBytes(bytes);
        String encoded = Base64.getEncoder().encodeToString(bytes);
        atomicWrite(configured, encoded.getBytes(StandardCharsets.UTF_8));
        try {
            Files.setPosixFilePermissions(configured, java.util.Set.of(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            // Windows 文件系统没有 POSIX 权限模型，依靠卷权限保护。
        }
        return encoded;
    }

    private static byte[] decodeKey(String encoded) {
        byte[] key = Base64.getDecoder().decode(encoded);
        if (key.length != MASTER_KEY_BYTES) {
            throw new IllegalArgumentException("identity master key must be 32 bytes");
        }
        return key;
    }

    private static byte[] decodePublicKey(String encoded) {
        try {
            return Base64.getUrlDecoder().decode(encoded);
        } catch (IllegalArgumentException ignored) {
            return Base64.getDecoder().decode(encoded);
        }
    }

    private static String encrypt(byte[] plain, byte[] key) throws GeneralSecurityException {
        byte[] iv = new byte[12];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] encrypted = cipher.doFinal(plain);
        byte[] payload = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, payload, 0, iv.length);
        System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(payload);
    }

    private static byte[] decrypt(String encoded, byte[] key) throws GeneralSecurityException {
        byte[] payload = Base64.getDecoder().decode(encoded);
        byte[] iv = java.util.Arrays.copyOf(payload, 12);
        byte[] encrypted = java.util.Arrays.copyOfRange(payload, 12, payload.length);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
        return cipher.doFinal(encrypted);
    }

    private static String fingerprint(PublicKey key) {
        return sha256Hex(key.getEncoded());
    }

    private static String sha256Hex(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String randomToken(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static Path path(String value) {
        return Path.of(value == null || value.isBlank() ? "config/tigshop" : value);
    }

    private static Path markerPath(Path identityPath) {
        return identityPath.resolveSibling(identityPath.getFileName() + ".initialized");
    }

    private static void atomicWrite(Path path, byte[] bytes) throws IOException {
        Path absolute = path.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = Files.createTempFile(parent, ".tigshop-identity-", ".tmp");
        try {
            Files.write(temp, bytes, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public record SignatureHeaders(String installationId, String packageDistributionId,
                                   String keyId, String timestamp, String nonce, String requestId,
                                   String signature, String canonical) {
    }

    private record PackageManifest(String packageDistributionId, String packageDistributionProof) {
    }

    public record IdentitySnapshot(String installationId, String packageDistributionId,
                                   String previousPackageDistributionId,
                                   String packageDistributionProof, String rootDomain,
                                   String publicKey, String encryptedPrivateKey, String keyFingerprint,
                                   boolean registered, String status, String lastSyncedVersion,
                                   long createdAt, PrivateKey privateKey) {
        static IdentitySnapshot unavailable(String status) {
            return new IdentitySnapshot("", "", "", "", "", "", "", "", false, status, "", 0, null);
        }

        public boolean available() {
            return privateKey != null && !installationId.isBlank() && !packageDistributionId.isBlank();
        }

        IdentitySnapshot withSync(String version) {
            return new IdentitySnapshot(installationId, packageDistributionId, "", packageDistributionProof, rootDomain, publicKey,
                    encryptedPrivateKey, keyFingerprint, true, "SYNCED", version, createdAt, privateKey);
        }

        IdentitySnapshot withRootDomain(String rootDomain) {
            return new IdentitySnapshot(installationId, packageDistributionId, previousPackageDistributionId,
                    packageDistributionProof, rootDomain == null ? "" : rootDomain, publicKey,
                    encryptedPrivateKey, keyFingerprint, registered, status, lastSyncedVersion,
                    createdAt, privateKey);
        }

        IdentitySnapshot withPackage(String packageDistributionId, String packageDistributionProof) {
            return new IdentitySnapshot(installationId, packageDistributionId, this.packageDistributionId,
                    packageDistributionProof, rootDomain, publicKey, encryptedPrivateKey, keyFingerprint,
                    registered, status, lastSyncedVersion, createdAt, privateKey);
        }
    }
}
