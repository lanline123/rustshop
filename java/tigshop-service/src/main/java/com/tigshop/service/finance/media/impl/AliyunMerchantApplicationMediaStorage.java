package com.tigshop.service.finance.media.impl;

import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.media.MerchantApplicationMediaStorage;
import com.tigshop.service.setting.OssService;
import com.tigshop.service.setting.CosService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.QiniuService;
import com.tigshop.service.setting.AwsS3Service;
import com.tigshop.service.setting.LocalStorageService;
import com.tigshop.bean.enums.common.StorageType;
import static com.tigshop.bean.enums.setting.SettingsEnum.STORAGE_TYPE;
import java.io.File;
import java.nio.file.Files;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * 阿里云商户进件媒体存储实现
 *
 * @author Tigshop团队
 */
@Component
public class AliyunMerchantApplicationMediaStorage implements MerchantApplicationMediaStorage {

    private static final String OBJECT_PREFIX = "merchant-application-media/";
    private static final Pattern OBJECT_NAME_PATTERN = Pattern.compile(
            "^" + Pattern.quote(OBJECT_PREFIX) + "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-"
                    + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final String STORAGE_ERROR_MESSAGE = "微信进件证件存储操作失败";

    private final OssService ossService;
    private final CosService cosService;
    private final ConfigService configService;
    private final QiniuService qiniuService;
    private final AwsS3Service awsS3Service;
    private final LocalStorageService localStorageService;

    public AliyunMerchantApplicationMediaStorage(OssService ossService) {
        this(ossService, null, null, null, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AliyunMerchantApplicationMediaStorage(OssService ossService, CosService cosService,
                                                 ConfigService configService, QiniuService qiniuService,
                                                 AwsS3Service awsS3Service, LocalStorageService localStorageService) {
        this.ossService = ossService;
        this.cosService = cosService;
        this.configService = configService;
        this.qiniuService = qiniuService;
        this.awsS3Service = awsS3Service;
        this.localStorageService = localStorageService;
    }

    @Override
    public void put(String objectName, byte[] content, String contentType) {
        validateObjectName(objectName);
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            if (isCosStorage()) {
                cosService.uploadFileStream(objectName, inputStream);
            } else if (isQiniuStorage()) {
                qiniuService.uploadFileStream(objectName, inputStream);
            } else if (isS3Storage()) {
                awsS3Service.uploadFileStream(objectName, inputStream);
            } else if (isLocalStorage()) {
                localStorageService.upload(inputStream, objectName);
            } else {
                ossService.uploadFile(inputStream, objectName);
            }
        } catch (Exception exception) {
            throw new GlobalException(STORAGE_ERROR_MESSAGE);
        }
    }

    @Override
    public byte[] get(String objectName) {
        validateObjectName(objectName);
        if (isCosStorage()) {
            try {
                File temp = Files.createTempFile("merchant-media-", ".bin").toFile();
                cosService.downloadFile(objectName, temp);
                try {
                    return Files.readAllBytes(temp.toPath());
                } finally {
                    Files.deleteIfExists(temp.toPath());
                }
            } catch (IOException | RuntimeException exception) {
                throw new GlobalException(STORAGE_ERROR_MESSAGE);
            }
        }
        if (isQiniuStorage()) {
            try (InputStream inputStream = qiniuService.downloadFile(objectName)) {
                return inputStream.readAllBytes();
            } catch (IOException | RuntimeException exception) {
                throw new GlobalException(STORAGE_ERROR_MESSAGE);
            }
        }
        if (isS3Storage()) {
            try (InputStream inputStream = awsS3Service.downloadFile(objectName)) {
                return inputStream.readAllBytes();
            } catch (IOException | RuntimeException exception) {
                throw new GlobalException(STORAGE_ERROR_MESSAGE);
            }
        }
        try (InputStream inputStream = isLocalStorage()
                ? localStorageService.download(objectName)
                : ossService.downloadFile(objectName)) {
            return inputStream.readAllBytes();
        } catch (IOException | RuntimeException exception) {
            throw new GlobalException(STORAGE_ERROR_MESSAGE);
        }
    }

    @Override
    public void delete(String objectName) {
        validateObjectName(objectName);
        try {
            if (isCosStorage()) {
                cosService.deleteFile(objectName);
            } else if (isQiniuStorage()) {
                qiniuService.deleteFile(objectName);
            } else if (isS3Storage()) {
                awsS3Service.deleteFile(objectName);
            } else if (isLocalStorage()) {
                localStorageService.delete(objectName);
            } else {
                ossService.deleteFile(objectName);
            }
        } catch (RuntimeException exception) {
            throw new GlobalException(STORAGE_ERROR_MESSAGE);
        }
    }

    private boolean isCosStorage() {
        return configService != null && cosService != null
                && StorageType.STORAGE_COS.getCode().equals(configService.getConfigVal(STORAGE_TYPE));
    }

    private boolean isQiniuStorage() { return configService != null && qiniuService != null && isType(StorageType.STORAGE_QINIU); }
    private boolean isS3Storage() { return configService != null && awsS3Service != null && isType(StorageType.STORAGE_S3); }
    private boolean isLocalStorage() { return configService != null && localStorageService != null && isType(StorageType.STORAGE_LOCAL); }
    private boolean isType(StorageType type) { return type.getCode().equals(configService.getConfigVal(STORAGE_TYPE)); }

    private void validateObjectName(String objectName) {
        if (objectName == null || !OBJECT_NAME_PATTERN.matcher(objectName).matches()) {
            throw new GlobalException("非法的微信进件证件对象键");
        }
    }
}
