package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.enums.finance.MerchantApplicationMediaRole;
import com.tigshop.bean.enums.finance.MerchantApplicationMediaStatus;
import com.tigshop.bean.model.finance.MerchantApplicationMedia;
import com.tigshop.bean.model.finance.MerchantApplication;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.FileSecurityUtils;
import com.tigshop.mapper.finance.MerchantApplicationMediaMapper;
import com.tigshop.mapper.finance.MerchantApplicationMapper;
import com.tigshop.mapper.setting.GalleryPicMapper;
import com.tigshop.service.finance.MerchantApplicationMediaService;
import com.tigshop.service.finance.media.MerchantApplicationMediaStorage;
import com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 商户进件媒体 Service 实现
 *
 * @author Tigshop团队
 */
@Slf4j
@Service
public class MerchantApplicationMediaServiceImpl implements MerchantApplicationMediaService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final String OBJECT_PREFIX = "merchant-application-media/";
    private static final String FAILURE_STATE_PERSISTENCE_MESSAGE = "上传失败，失败状态持久化失败";
    private static final String UPLOAD_FAILED_ERROR_CODE = "UPLOAD_FAILED";
    private static final String GALLERY_OBJECT_PREFIX = "img/gallery/";
    private static final String APPLICANT_SHOP = "SHOP";
    private static final String APPLICANT_VENDOR = "VENDOR";
    private static final String MEDIA_OWNER_PLATFORM = "PLATFORM";
    /**
     * 已绑定证件的「可重传窗口」，而非保留期。
     *
     * <p>该时间只用于判断证件能否再次上传给渠道（见 {@code requireBoundMedia} 的 requireUnexpired），
     * 系统中没有任何按 expire_time 扫描删除的任务，过期不会删文件。
     * 证件的实际存亡只取决于是否调用清理——业务方要求永久保留，故已移除提交成功后的清理调用。
     */
    private static final Duration BOUND_RETENTION = Duration.ofDays(7);
    private static final Map<String, String> EXTENSION_CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "bmp", "image/bmp");

    private final MerchantApplicationMediaMapper mediaMapper;
    private final MerchantApplicationMapper applicationMapper;
    private final MerchantApplicationMediaStorage storage;
    private final ProfitSharingClientRegistry profitSharingClientRegistry;
    private final GalleryPicMapper galleryPicMapper;
    private final OssService ossService;
    private final ConfigService configService;
    private final Clock clock;

    @Autowired
    public MerchantApplicationMediaServiceImpl(
            MerchantApplicationMediaMapper mediaMapper,
            MerchantApplicationMapper applicationMapper,
            MerchantApplicationMediaStorage storage,
            ProfitSharingClientRegistry profitSharingClientRegistry,
            GalleryPicMapper galleryPicMapper,
            OssService ossService,
            ConfigService configService) {
        this(mediaMapper, applicationMapper, storage, profitSharingClientRegistry, galleryPicMapper, ossService,
                configService, Clock.systemDefaultZone());
    }

    MerchantApplicationMediaServiceImpl(
            MerchantApplicationMediaMapper mediaMapper,
            MerchantApplicationMapper applicationMapper,
            MerchantApplicationMediaStorage storage,
            ProfitSharingClientRegistry profitSharingClientRegistry,
            GalleryPicMapper galleryPicMapper,
            OssService ossService,
            ConfigService configService,
            Clock clock) {
        this.mediaMapper = mediaMapper;
        this.applicationMapper = applicationMapper;
        this.storage = storage;
        this.profitSharingClientRegistry = profitSharingClientRegistry;
        this.galleryPicMapper = galleryPicMapper;
        this.ossService = ossService;
        this.configService = configService;
        this.clock = clock;
    }

    @Override
    public String uploadMedia(MultipartFile file, Integer uploaderAdminId) {
        if (uploaderAdminId == null || uploaderAdminId <= 0) {
            throw new GlobalException("无效的上传账号");
        }

        return uploadValidatedImage(validateImage(file), uploaderAdminId).mediaId();
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            noRollbackFor = GlobalException.class)
    public List<String> normalizeSubmissionMedia(
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId,
            String licenseValue,
            String portraitValue,
            String nationalValue) {
        validateNormalizationContext(applicantType, applicantId, uploaderAdminId);
        List<String> values = List.of(
                normalizeMediaValue(licenseValue),
                normalizeMediaValue(portraitValue),
                normalizeMediaValue(nationalValue));
        StorageOrigin storageOrigin = resolveStorageOrigin();
        List<NormalizationCandidate> candidates = values.stream()
                .map(value -> prepareCandidate(
                        value, applicantType, applicantId, uploaderAdminId, storageOrigin))
                .toList();
        List<String> normalized = new ArrayList<>(candidates.size());
        List<UploadedMedia> uploadedMedia = new ArrayList<>(candidates.size());
        try {
            for (NormalizationCandidate candidate : candidates) {
                if (candidate.image() == null) {
                    normalized.add(candidate.value());
                    continue;
                }
                UploadedMedia uploaded = uploadValidatedImage(candidate.image(), uploaderAdminId);
                uploadedMedia.add(uploaded);
                normalized.add(uploaded.mediaId());
            }
        } catch (GlobalException exception) {
            uploadedMedia.forEach(this::discardImportedMediaQuietly);
            throw exception;
        }
        return normalized;
    }

    private UploadedMedia uploadValidatedImage(ValidatedImage image, Integer uploaderAdminId) {
        String objectName = OBJECT_PREFIX + UUID.randomUUID();
        Date now = Date.from(clock.instant());
        MerchantApplicationMedia record = buildUploadingRecord(
                uploaderAdminId, objectName, image, now);

        try {
            if (mediaMapper.insert(record) != 1) {
                throw new GlobalException("创建进件媒体上传记录失败");
            }
        } catch (Exception exception) {
            log.error("创建进件媒体上传记录失败");
            throw new GlobalException("上传媒体文件失败，请稍后重试");
        }

        try {
            storage.put(objectName, image.content(), image.contentType());
            String serverFileName = UUID.randomUUID() + "." + image.extension();
            String mediaId = wechatClient().uploadMedia(image.content(), serverFileName);
            if (mediaId == null || mediaId.isBlank()) {
                throw new GlobalException("微信媒体上传未返回有效结果");
            }

            record.setInitialMediaIdHash(sha256(mediaId));
            record.setStatus(MerchantApplicationMediaStatus.TEMP.getCode());
            record.setExpireTime(Date.from(clock.instant().plus(Duration.ofHours(24))));
            record.setModifyTime(Date.from(clock.instant()));
            if (mediaMapper.updateById(record) != 1) {
                throw new GlobalException("更新进件媒体临时状态失败");
            }
            return new UploadedMedia(mediaId, record);
        } catch (Exception exception) {
            try {
                markDeletePending(record);
            } finally {
                deleteStoredObjectQuietly(objectName);
            }
            log.error("进件媒体上传失败, mediaRecordId={}", record.getId());
            throw new GlobalException("上传媒体文件失败，请稍后重试");
        }
    }

    @Override
    public boolean bindTempMedia(
            Long applicationId,
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId,
            String licenseMediaId,
            String portraitMediaId,
            String nationalMediaId) {
        validateBindingContext(applicationId, applicantType, applicantId, uploaderAdminId);
        List<MediaBinding> bindings = new ArrayList<>();
        if (!isPersonalApplication(applicationId)) {
            bindings.add(new MediaBinding(licenseMediaId, MerchantApplicationMediaRole.BUSINESS_LICENSE));
        }
        bindings.add(new MediaBinding(portraitMediaId, MerchantApplicationMediaRole.ID_CARD_PORTRAIT));
        bindings.add(new MediaBinding(nationalMediaId, MerchantApplicationMediaRole.ID_CARD_NATIONAL));

        List<MerchantApplicationMedia> oldBoundMedia = findBoundMedia(applicationId);
        List<MerchantApplicationMedia> trackedMedia = bindings.stream()
                .map(binding -> findOwnedTempMedia(binding.mediaId(), uploaderAdminId, true))
                .toList();
        long trackedCount = trackedMedia.stream().filter(Objects::nonNull).count();
        if (trackedCount == 0 && bindings.stream().noneMatch(this::hasTrackingRecord)) {
            return false;
        }
        if (trackedCount != bindings.size()) {
            throw new GlobalException("进件媒体无效或已失效，请重新上传");
        }

        Date now = Date.from(clock.instant());
        Date expireTime = Date.from(clock.instant().plus(BOUND_RETENTION));
        for (MerchantApplicationMedia media : trackedMedia) {
            validateTempMedia(media, uploaderAdminId, now);
        }
        for (int index = 0; index < bindings.size(); index++) {
            MediaBinding binding = bindings.get(index);
            MerchantApplicationMedia media = trackedMedia.get(index);
            bindOne(media, binding.role(), applicationId, applicantType, applicantId, expireTime, now);
        }
        for (MerchantApplicationMedia oldMedia : oldBoundMedia) {
            markOldMediaDeletePending(oldMedia, applicationId, now);
        }
        return true;
    }

    @Override
    public List<String> reuploadBoundMedia(Long applicationId) {
        List<MerchantApplicationMedia> media = requireBoundMedia(applicationId, true);
        try {
            return media.stream().map(this::reuploadOne).toList();
        } catch (Exception exception) {
            log.error("进件媒体重传失败, applicationId={}", applicationId);
            throw new MerchantApplicationMediaService.ReuploadFailedException();
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void extendBoundMediaExpiration(Long applicationId) {
        extendBoundMediaExpirationInternal(applicationId);
    }

    @Override
    public void extendBoundMediaExpirationInCurrentTransaction(Long applicationId) {
        extendBoundMediaExpirationInternal(applicationId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void cleanupBoundMediaInNewTransaction(Long applicationId) {
        cleanupBoundMedia(applicationId);
    }

    private void extendBoundMediaExpirationInternal(Long applicationId) {
        List<MerchantApplicationMedia> media = requireBoundMedia(applicationId, false);
        Date now = Date.from(clock.instant());
        Date expireTime = Date.from(clock.instant().plus(BOUND_RETENTION));
        for (MerchantApplicationMedia record : media) {
            LambdaUpdateWrapper<MerchantApplicationMedia> wrapper = boundRecordUpdate(record, applicationId)
                    .set(MerchantApplicationMedia::getStatus,
                            MerchantApplicationMediaStatus.BOUND.getCode())
                    .set(MerchantApplicationMedia::getExpireTime, expireTime)
                    .set(MerchantApplicationMedia::getModifyTime, now);
            requireSingleRow(mediaMapper.update(null, wrapper), "进件媒体延期失败");
            record.setStatus(MerchantApplicationMediaStatus.BOUND.getCode());
            record.setExpireTime(expireTime);
            record.setModifyTime(now);
        }
    }

    private ValidatedImage validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GlobalException("文件大小不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileSecurityUtils.getFileExtension(originalFilename);
        String expectedContentType = EXTENSION_CONTENT_TYPES.get(extension);
        String declaredContentType = file.getContentType();
        if (expectedContentType == null || declaredContentType == null
                || !expectedContentType.equals(declaredContentType.trim().toLowerCase(Locale.ROOT))) {
            throw new GlobalException("文件扩展名与类型不一致");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new GlobalException("读取上传文件失败");
        }
        return validateImage(extension, declaredContentType, content);
    }

    private ValidatedImage validateImage(
            String extension,
            String declaredContentType,
            byte[] content) {
        String normalizedExtension = extension == null
                ? "" : extension.trim().toLowerCase(Locale.ROOT);
        String expectedContentType = EXTENSION_CONTENT_TYPES.get(normalizedExtension);
        String normalizedContentType = declaredContentType == null
                ? "" : declaredContentType.trim().toLowerCase(Locale.ROOT);
        if (expectedContentType == null || !expectedContentType.equals(normalizedContentType)) {
            throw new GlobalException("文件扩展名与类型不一致");
        }
        if (content.length == 0 || content.length > MAX_FILE_SIZE) {
            throw new GlobalException(content.length == 0 ? "上传文件不能为空" : "文件大小不能超过5MB");
        }

        String detectedContentType = FileSecurityUtils.detectFileType(new ByteArrayInputStream(content));
        if (!expectedContentType.equals(detectedContentType)) {
            throw new GlobalException("文件内容与声明类型不一致");
        }
        return new ValidatedImage(normalizedExtension, expectedContentType, content);
    }

    private void validateNormalizationContext(
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId) {
        // applicantId 允许为 0：平台自营的进件主体就是 shopId=0 这一虚拟门店
        if ((!APPLICANT_SHOP.equals(applicantType)
                && !APPLICANT_VENDOR.equals(applicantType)
                && !MEDIA_OWNER_PLATFORM.equals(applicantType))
                || applicantId == null || applicantId < 0 || applicantId > Integer.MAX_VALUE
                || uploaderAdminId == null || uploaderAdminId <= 0) {
            throw new GlobalException("进件媒体归一化上下文无效");
        }
    }

    private StorageOrigin resolveStorageOrigin() {
        String storageType;
        String storageUrl;
        try {
            storageType = configService.getConfigVal(STORAGE_TYPE);
            StorageType type = StorageType.fromTypeCode(storageType);
            storageUrl = configService.getConfigVal(type.getSettingsEnum());
        } catch (RuntimeException exception) {
            throw invalidInternalMedia();
        }
        if (storageUrl == null || storageUrl.isBlank()) {
            throw invalidInternalMedia();
        }
        try {
            URI uri = new URI(storageUrl.trim());
            validateStorageUri(uri);
            return new StorageOrigin(uri.getScheme().toLowerCase(Locale.ROOT),
                    uri.getHost().toLowerCase(Locale.ROOT), effectivePort(uri),
                    normalizeBasePath(uri.getRawPath()));
        } catch (URISyntaxException exception) {
            throw invalidInternalMedia();
        }
    }

    private NormalizationCandidate prepareCandidate(
            String value,
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId,
            StorageOrigin storageOrigin) {
        MerchantApplicationMedia tracked = findOwnedTempMedia(value, uploaderAdminId, false);
        if (tracked != null) {
            validateTempMedia(tracked, uploaderAdminId, Date.from(clock.instant()));
            return new NormalizationCandidate(value, null);
        }
        if (!isHttpUrl(value)) {
            return new NormalizationCandidate(value, null);
        }

        GalleryPic galleryPic = findOwnedGalleryPic(
                value, applicantType, applicantId.intValue(), uploaderAdminId);
        String objectName = resolveObjectName(galleryPic.getPicUrl(), storageOrigin);
        byte[] content = downloadGalleryObject(objectName);
        String extension = galleryPic.getFileType();
        if (extension == null || extension.isBlank()) {
            extension = FileSecurityUtils.getFileExtension(objectName);
        }
        String contentType = EXTENSION_CONTENT_TYPES.get(
                extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT));
        ValidatedImage image = validateImage(extension, contentType, content);
        return new NormalizationCandidate(value, image);
    }

    private GalleryPic findOwnedGalleryPic(
            String value,
            String applicantType,
            Integer applicantId,
            Integer uploaderAdminId) {
        LambdaQueryWrapper<GalleryPic> query = new LambdaQueryWrapper<GalleryPic>()
                .eq(GalleryPic::getPicUrl, value)
                .eq(GalleryPic::getPicOwerId, uploaderAdminId);
        if (MEDIA_OWNER_PLATFORM.equals(applicantType)) {
            // 与 SHOP/VENDOR 分支同口径按「非外链」筛：图库普通上传不写 source（留 null），
            // 只有外部图片才置为 external，因此不能按 source='upload' 正向匹配——那个值系统从不产生
            query.eq(GalleryPic::getShopId, 0)
                    .eq(GalleryPic::getVendorId, 0)
                    .and(wrapper -> wrapper.isNull(GalleryPic::getSource)
                            .or().ne(GalleryPic::getSource, "external"));
        } else if (APPLICANT_VENDOR.equals(applicantType)) {
            query.and(wrapper -> wrapper.isNull(GalleryPic::getSource)
                            .or().ne(GalleryPic::getSource, "external"))
                    .eq(GalleryPic::getVendorId, applicantId);
        } else {
            query.and(wrapper -> wrapper.isNull(GalleryPic::getSource)
                            .or().ne(GalleryPic::getSource, "external"))
                    .eq(GalleryPic::getShopId, applicantId);
        }
        List<GalleryPic> matches = galleryPicMapper.selectList(query.last("limit 2"));
        if (matches == null || matches.size() != 1) {
            throw invalidInternalMedia();
        }
        GalleryPic match = matches.getFirst();
        boolean ownedByApplicant;
        if (MEDIA_OWNER_PLATFORM.equals(applicantType)) {
            // 外链在下方统一复核，此处只判归属，口径与查询条件保持一致
            ownedByApplicant = Objects.equals(match.getShopId(), 0)
                    && Objects.equals(match.getVendorId(), 0);
        } else if (APPLICANT_VENDOR.equals(applicantType)) {
            ownedByApplicant = Objects.equals(match.getVendorId(), applicantId);
        } else {
            ownedByApplicant = Objects.equals(match.getShopId(), applicantId);
        }
        if (!Objects.equals(match.getPicUrl(), value)
                || !Objects.equals(match.getPicOwerId(), uploaderAdminId)
                || "external".equalsIgnoreCase(match.getSource())
                || !ownedByApplicant) {
            throw invalidInternalMedia();
        }
        return match;
    }

    private String resolveObjectName(String mediaUrl, StorageOrigin storageOrigin) {
        try {
            URI uri = new URI(mediaUrl);
            validateStorageUri(uri);
            if (!storageOrigin.scheme().equals(uri.getScheme().toLowerCase(Locale.ROOT))
                    || !storageOrigin.host().equals(uri.getHost().toLowerCase(Locale.ROOT))
                    || storageOrigin.port() != effectivePort(uri)) {
                throw invalidInternalMedia();
            }
            String rawPath = uri.getRawPath();
            if (rawPath == null || rawPath.indexOf('%') >= 0 || rawPath.indexOf('\\') >= 0
                    || !rawPath.startsWith(storageOrigin.basePath())) {
                throw invalidInternalMedia();
            }
            String objectName = rawPath.substring(storageOrigin.basePath().length());
            if (objectName.startsWith("/")) {
                objectName = objectName.substring(1);
            }
            if (!objectName.startsWith(GALLERY_OBJECT_PREFIX)
                    || objectName.contains("//") || hasUnsafePathSegment(objectName)) {
                throw invalidInternalMedia();
            }
            return objectName;
        } catch (URISyntaxException exception) {
            throw invalidInternalMedia();
        }
    }

    private byte[] downloadGalleryObject(String objectName) {
        try (InputStream input = downloadGalleryObjectStream(objectName)) {
            if (input == null) {
                throw invalidInternalMedia();
            }
            byte[] content = input.readNBytes((int) MAX_FILE_SIZE + 1);
            if (content.length == 0 || content.length > MAX_FILE_SIZE) {
                throw invalidInternalMedia();
            }
            return content;
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof GlobalException globalException) {
                throw globalException;
            }
            throw invalidInternalMedia();
        }
    }

    private InputStream downloadGalleryObjectStream(String objectName) throws IOException {
        String storageType = configService.getConfigVal(STORAGE_TYPE);
        StorageType type = StorageType.fromTypeCode(storageType);
        if (type == StorageType.STORAGE_OSS) {
            return ossService.downloadFile(objectName);
        }
        String baseUrl = configService.getConfigVal(type.getSettingsEnum());
        if (baseUrl == null || baseUrl.isBlank()) {
            throw invalidInternalMedia();
        }
        String normalized = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return new URL(normalized + objectName).openStream();
    }

    private void validateStorageUri(URI uri) {
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null || uri.getHost().isBlank()
                || uri.getUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            throw invalidInternalMedia();
        }
    }

    private int effectivePort(URI uri) {
        return uri.getPort() < 0 ? 443 : uri.getPort();
    }

    private String normalizeBasePath(String rawPath) {
        String basePath = rawPath == null || rawPath.isBlank() ? "/" : rawPath;
        if (basePath.indexOf('%') >= 0 || basePath.indexOf('\\') >= 0
                || hasUnsafePathSegment(basePath)) {
            throw invalidInternalMedia();
        }
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        return basePath.endsWith("/") ? basePath : basePath + "/";
    }

    private boolean hasUnsafePathSegment(String path) {
        for (String segment : path.split("/", -1)) {
            if (".".equals(segment) || "..".equals(segment)
                    || segment.chars().anyMatch(character -> character < 0x20 || character == 0x7f)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHttpUrl(String value) {
        String lowerValue = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return lowerValue.startsWith("http://") || lowerValue.startsWith("https://");
    }

    private String normalizeMediaValue(String value) {
        return value == null ? "" : value.trim();
    }

    private GlobalException invalidInternalMedia() {
        return new GlobalException("进件媒体无效，请重新上传");
    }

    private void validateBindingContext(
            Long applicationId,
            String applicantType,
            Long applicantId,
            Integer uploaderAdminId) {
        // applicantId 允许为 0：平台自营的进件主体就是 shopId=0 这一虚拟门店
        if (applicationId == null || applicationId <= 0
                || applicantType == null || applicantType.isBlank()
                || applicantId == null || applicantId < 0
                || uploaderAdminId == null || uploaderAdminId <= 0) {
            throw new GlobalException("进件媒体绑定上下文无效");
        }
    }

    private MerchantApplicationMedia findOwnedTempMedia(
            String mediaId,
            Integer uploaderAdminId,
            boolean currentRead) {
        if (mediaId == null || mediaId.isBlank()) {
            return null;
        }
        LambdaQueryWrapper<MerchantApplicationMedia> query =
                new LambdaQueryWrapper<MerchantApplicationMedia>()
                .eq(MerchantApplicationMedia::getInitialMediaIdHash, sha256(mediaId))
                .eq(MerchantApplicationMedia::getUploaderAdminId, uploaderAdminId)
                .eq(MerchantApplicationMedia::getStatus, MerchantApplicationMediaStatus.TEMP.getCode())
                .eq(MerchantApplicationMedia::getIsDel, 0);
        if (currentRead) {
            query.last("FOR UPDATE");
        }
        return mediaMapper.selectOne(query);
    }

    private boolean hasTrackingRecord(MediaBinding binding) {
        String mediaHash = sha256(binding.mediaId());
        LambdaQueryWrapper<MerchantApplicationMedia> activeQuery = trackingExistenceQuery(mediaHash)
                .eq(MerchantApplicationMedia::getIsDel, 0);
        if (trackingRecordExists(activeQuery)) {
            return true;
        }
        LambdaQueryWrapper<MerchantApplicationMedia> deletedQuery = trackingExistenceQuery(mediaHash)
                .eq(MerchantApplicationMedia::getIsDel, 1);
        return trackingRecordExists(deletedQuery);
    }

    private LambdaQueryWrapper<MerchantApplicationMedia> trackingExistenceQuery(String mediaHash) {
        return new LambdaQueryWrapper<MerchantApplicationMedia>()
                .eq(MerchantApplicationMedia::getInitialMediaIdHash, mediaHash);
    }

    private boolean trackingRecordExists(
            LambdaQueryWrapper<MerchantApplicationMedia> queryWrapper) {
        queryWrapper.select(MerchantApplicationMedia::getId);
        return !mediaMapper.selectList(queryWrapper).isEmpty();
    }

    private void validateTempMedia(
            MerchantApplicationMedia media,
            Integer uploaderAdminId,
            Date now) {
        if (!Objects.equals(media.getUploaderAdminId(), uploaderAdminId)
                || !Objects.equals(media.getStatus(), MerchantApplicationMediaStatus.TEMP.getCode())
                || !Objects.equals(media.getIsDel(), 0)
                || media.getExpireTime() == null
                || !media.getExpireTime().after(now)) {
            throw new GlobalException("进件媒体无效或已失效，请重新上传");
        }
    }

    private void bindOne(
            MerchantApplicationMedia media,
            MerchantApplicationMediaRole role,
            Long applicationId,
            String applicantType,
            Long applicantId,
            Date expireTime,
            Date now) {
        LambdaUpdateWrapper<MerchantApplicationMedia> wrapper =
                new LambdaUpdateWrapper<MerchantApplicationMedia>()
                        .eq(MerchantApplicationMedia::getId, media.getId())
                        .eq(MerchantApplicationMedia::getUploaderAdminId, media.getUploaderAdminId())
                        .eq(MerchantApplicationMedia::getInitialMediaIdHash,
                                media.getInitialMediaIdHash())
                        .eq(MerchantApplicationMedia::getStatus,
                                MerchantApplicationMediaStatus.TEMP.getCode())
                        .eq(MerchantApplicationMedia::getIsDel, 0)
                        .set(MerchantApplicationMedia::getApplicationId, applicationId)
                        .set(MerchantApplicationMedia::getApplicantType, applicantType)
                        .set(MerchantApplicationMedia::getApplicantId, applicantId)
                        .set(MerchantApplicationMedia::getMediaRole, role.getCode())
                        .set(MerchantApplicationMedia::getStatus,
                                MerchantApplicationMediaStatus.BOUND.getCode())
                        .set(MerchantApplicationMedia::getExpireTime, expireTime)
                        .set(MerchantApplicationMedia::getModifyTime, now);
        requireSingleRow(mediaMapper.update(null, wrapper), "进件媒体绑定失败");
        media.setApplicationId(applicationId);
        media.setApplicantType(applicantType);
        media.setApplicantId(applicantId);
        media.setMediaRole(role.getCode());
        media.setStatus(MerchantApplicationMediaStatus.BOUND.getCode());
        media.setExpireTime(expireTime);
        media.setModifyTime(now);
    }

    @Override
    public MediaContent loadBoundMediaContent(
            Long applicationId, String applicantType, Long applicantId, Integer roleCode) {
        // applicantId 允许为 0：平台自营的进件主体就是 shopId=0 这一虚拟门店
        if (applicationId == null || applicationId <= 0 || roleCode == null
                || StrUtil.isBlank(applicantType) || applicantId == null || applicantId < 0) {
            return null;
        }
        MerchantApplicationMediaRole role = resolveRole(roleCode);
        if (role == null || role == MerchantApplicationMediaRole.UNBOUND) {
            return null;
        }
        MerchantApplicationMedia media = mediaMapper.selectOne(
                new LambdaQueryWrapper<MerchantApplicationMedia>()
                        .eq(MerchantApplicationMedia::getApplicationId, applicationId)
                        .eq(MerchantApplicationMedia::getApplicantType, applicantType)
                        .eq(MerchantApplicationMedia::getApplicantId, applicantId)
                        .eq(MerchantApplicationMedia::getMediaRole, role.getCode())
                        .eq(MerchantApplicationMedia::getStatus, MerchantApplicationMediaStatus.BOUND.getCode())
                        .eq(MerchantApplicationMedia::getIsDel, 0));
        if (media == null || StrUtil.isBlank(media.getObjectName())) {
            // 媒体有保留期，过期后会被清理任务删除，历史单据取不到属正常；归属不符同样按取不到处理，不区分响应
            log.info("[进件媒体] 预览取不到已绑定媒体, applicationId={}, role={}", applicationId, roleCode);
            return null;
        }
        byte[] content = storage.get(media.getObjectName());
        if (content == null || content.length == 0) {
            log.warn("[进件媒体] 预览读取到空内容, applicationId={}, role={}", applicationId, roleCode);
            return null;
        }
        String contentType = StrUtil.isNotBlank(media.getContentType())
                ? media.getContentType()
                : "application/octet-stream";
        return new MediaContent(content, contentType);
    }

    private MerchantApplicationMediaRole resolveRole(Integer roleCode) {
        for (MerchantApplicationMediaRole role : MerchantApplicationMediaRole.values()) {
            if (role.getCode().equals(roleCode)) {
                return role;
            }
        }
        return null;
    }

    private List<MerchantApplicationMedia> findBoundMedia(Long applicationId) {
        return mediaMapper.selectList(new LambdaQueryWrapper<MerchantApplicationMedia>()
                .eq(MerchantApplicationMedia::getApplicationId, applicationId)
                .eq(MerchantApplicationMedia::getStatus, MerchantApplicationMediaStatus.BOUND.getCode())
                .eq(MerchantApplicationMedia::getIsDel, 0));
    }

    private List<MerchantApplicationMedia> requireBoundMedia(
            Long applicationId,
            boolean requireUnexpired) {
        List<MerchantApplicationMediaRole> roles = new ArrayList<>();
        if (!isPersonalApplication(applicationId)) {
            roles.add(MerchantApplicationMediaRole.BUSINESS_LICENSE);
        }
        roles.add(MerchantApplicationMediaRole.ID_CARD_PORTRAIT);
        roles.add(MerchantApplicationMediaRole.ID_CARD_NATIONAL);
        List<MerchantApplicationMedia> media;
        try {
            media = new ArrayList<>(roles.size());
            for (MerchantApplicationMediaRole role : roles) {
                media.add(findBoundMediaByRole(applicationId, role));
            }
        } catch (Exception exception) {
            log.error("进件媒体角色查询失败, applicationId={}", applicationId);
            throw new GlobalException("申请证件异常，请重新上传后再提交");
        }
        if (media.stream().anyMatch(Objects::isNull)) {
            throw new GlobalException("申请证件缺失，请重新上传后再提交");
        }
        Date now = Date.from(clock.instant());
        for (int index = 0; index < media.size(); index++) {
            MerchantApplicationMedia record = media.get(index);
            MerchantApplicationMediaRole role = roles.get(index);
            if (!Objects.equals(record.getApplicationId(), applicationId)
                    || !Objects.equals(record.getMediaRole(), role.getCode())
                    || !Objects.equals(record.getStatus(), MerchantApplicationMediaStatus.BOUND.getCode())
                    || !Objects.equals(record.getIsDel(), 0)
                    || (requireUnexpired && (record.getExpireTime() == null
                    || !record.getExpireTime().after(now)))
                    || record.getObjectName() == null
                    || record.getObjectName().isBlank()) {
                throw new GlobalException("申请证件无效，请重新上传后再提交");
            }
        }
        return media;
    }

    private boolean isPersonalApplication(Long applicationId) {
        // 以已保存的申请主体为准，不能通过少传一张证件绕过企业/个体户的执照要求。
        MerchantApplication application = applicationMapper.selectById(applicationId);
        return application != null && "2401".equals(application.getOrganizationType())
                && "WECHAT".equals(application.getPayChannel());
    }

    private MerchantApplicationMedia findBoundMediaByRole(
            Long applicationId,
            MerchantApplicationMediaRole role) {
        return mediaMapper.selectOne(new LambdaQueryWrapper<MerchantApplicationMedia>()
                .eq(MerchantApplicationMedia::getApplicationId, applicationId)
                .eq(MerchantApplicationMedia::getMediaRole, role.getCode())
                .eq(MerchantApplicationMedia::getStatus, MerchantApplicationMediaStatus.BOUND.getCode())
                .eq(MerchantApplicationMedia::getIsDel, 0));
    }

    private String reuploadOne(MerchantApplicationMedia media) {
        byte[] content = storage.get(media.getObjectName());
        if (content == null || content.length == 0) {
            throw new GlobalException("进件媒体原始文件不存在");
        }
        String extension = media.getFileExt() == null ? "jpg" : media.getFileExt();
        String mediaId = wechatClient().uploadMedia(
                content, UUID.randomUUID() + "." + extension);
        if (mediaId == null || mediaId.isBlank()) {
            throw new GlobalException("微信媒体上传未返回有效结果");
        }
        return mediaId;
    }

    private com.tigshop.service.finance.profitsharing.ProfitSharingApiClient wechatClient() {
        return profitSharingClientRegistry.get("WECHAT");
    }

    private LambdaUpdateWrapper<MerchantApplicationMedia> boundRecordUpdate(
            MerchantApplicationMedia record,
            Long applicationId) {
        return new LambdaUpdateWrapper<MerchantApplicationMedia>()
                .eq(MerchantApplicationMedia::getId, record.getId())
                .eq(MerchantApplicationMedia::getApplicationId, applicationId)
                .eq(MerchantApplicationMedia::getMediaRole, record.getMediaRole())
                .eq(MerchantApplicationMedia::getStatus,
                        MerchantApplicationMediaStatus.BOUND.getCode())
                .eq(MerchantApplicationMedia::getIsDel, 0);
    }

    private void markOldMediaDeletePending(
            MerchantApplicationMedia record,
            Long applicationId,
            Date now) {
        LambdaUpdateWrapper<MerchantApplicationMedia> wrapper = boundRecordUpdate(record, applicationId)
                .set(MerchantApplicationMedia::getStatus,
                        MerchantApplicationMediaStatus.DELETE_PENDING.getCode())
                .set(MerchantApplicationMedia::getExpireTime, now)
                .set(MerchantApplicationMedia::getNextRetryTime, now)
                .set(MerchantApplicationMedia::getModifyTime, now);
        requireSingleRow(mediaMapper.update(null, wrapper), "旧进件媒体替换失败");
        record.setStatus(MerchantApplicationMediaStatus.DELETE_PENDING.getCode());
        record.setExpireTime(now);
        record.setNextRetryTime(now);
        record.setModifyTime(now);
    }

    private void cleanupBoundMedia(Long applicationId) {
        List<MerchantApplicationMedia> media;
        try {
            media = requireBoundMedia(applicationId, true);
        } catch (Exception exception) {
            log.error("进件媒体提交后清理准备失败, applicationId={}", applicationId);
            return;
        }
        Date now = Date.from(clock.instant());
        for (MerchantApplicationMedia record : media) {
            try {
                markOldMediaDeletePending(record, applicationId, now);
                storage.delete(record.getObjectName());
            } catch (Exception exception) {
                log.warn("进件媒体提交后清理失败, applicationId={}", applicationId);
            }
        }
    }

    private void requireSingleRow(int affectedRows, String message) {
        if (affectedRows != 1) {
            throw new GlobalException(message);
        }
    }

    private MerchantApplicationMedia buildUploadingRecord(
            Integer uploaderAdminId,
            String objectName,
            ValidatedImage image,
            Date now) {
        MerchantApplicationMedia record = new MerchantApplicationMedia();
        record.setApplicationId(0L);
        record.setUploaderAdminId(uploaderAdminId);
        record.setMediaRole(MerchantApplicationMediaRole.UNBOUND.getCode());
        record.setObjectName(objectName);
        record.setFileExt(image.extension());
        record.setContentType(image.contentType());
        record.setFileSize((long) image.content().length);
        record.setStatus(MerchantApplicationMediaStatus.UPLOADING.getCode());
        record.setRetryCount(0);
        record.setIsDel(0);
        record.setExpireTime(Date.from(now.toInstant().plus(Duration.ofHours(24))));
        record.setCreateTime(now);
        record.setModifyTime(now);
        return record;
    }

    private void markDeletePending(MerchantApplicationMedia record) {
        Date now = Date.from(clock.instant());
        LambdaUpdateWrapper<MerchantApplicationMedia> updateWrapper =
                new LambdaUpdateWrapper<MerchantApplicationMedia>()
                        .eq(MerchantApplicationMedia::getId, record.getId())
                        .eq(MerchantApplicationMedia::getIsDel, 0)
                        .set(MerchantApplicationMedia::getStatus,
                                MerchantApplicationMediaStatus.DELETE_PENDING.getCode())
                        .set(MerchantApplicationMedia::getModifyTime, now)
                        .set(MerchantApplicationMedia::getNextRetryTime, now)
                        .set(MerchantApplicationMedia::getLastErrorCode, UPLOAD_FAILED_ERROR_CODE)
                        .set(MerchantApplicationMedia::getExpireTime, now);

        int affectedRows;
        try {
            affectedRows = mediaMapper.update(null, updateWrapper);
        } catch (Exception exception) {
            log.error("进件媒体失败状态持久化失败, mediaRecordId={}", record.getId());
            throw new GlobalException(FAILURE_STATE_PERSISTENCE_MESSAGE);
        }
        if (affectedRows != 1) {
            log.error("进件媒体失败状态持久化失败, mediaRecordId={}", record.getId());
            throw new GlobalException(FAILURE_STATE_PERSISTENCE_MESSAGE);
        }
    }

    private void deleteStoredObjectQuietly(String objectName) {
        try {
            storage.delete(objectName);
        } catch (Exception ignored) {
            log.warn("进件媒体私有对象即时清理失败，等待补偿任务处理");
        }
    }

    private void discardImportedMediaQuietly(UploadedMedia uploaded) {
        try {
            markDeletePending(uploaded.record());
        } catch (Exception exception) {
            log.warn("进件媒体批量导入失败状态更新异常, mediaRecordId={}",
                    uploaded.record().getId());
        } finally {
            deleteStoredObjectQuietly(uploaded.record().getObjectName());
        }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new GlobalException("媒体摘要计算失败");
        }
    }

    private record ValidatedImage(String extension, String contentType, byte[] content) {
    }

    private record NormalizationCandidate(String value, ValidatedImage image) {
    }

    private record UploadedMedia(String mediaId, MerchantApplicationMedia record) {
    }

    private record StorageOrigin(String scheme, String host, int port, String basePath) {
    }

    private record MediaBinding(String mediaId, MerchantApplicationMediaRole role) {
    }

}
