// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.AwsS3Service;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import static com.tigshop.common.constant.ExceptionConstants.SYSTEM_ERROR;

/**
 * AWS S3 存储服务实现
 *
 * @author Tigshop团队
 * @create 2026年01月19日
 */
@Service
public class AwsS3ServiceImpl implements AwsS3Service {

    @Resource
    private ConfigService configService;

    private String storageS3AccessKey;
    private String storageS3SecretKey;
    private String storageS3Region;
    private String storageS3Bucket;
    private String storageS3Endpoint;

    /**
     * 获取配置信息
     */
    private void getConfigStorage() {
        storageS3AccessKey = configService.getConfigByCode(SettingsEnum.STORAGE_S3_ACCESS_KEY.getBizCode()).getBizVal();
        storageS3SecretKey = configService.getConfigByCode(SettingsEnum.STORAGE_S3_SECRET_KEY.getBizCode()).getBizVal();
        storageS3Region = configService.getConfigByCode(SettingsEnum.STORAGE_S3_REGION.getBizCode()).getBizVal();
        storageS3Bucket = configService.getConfigByCode(SettingsEnum.STORAGE_S3_BUCKET.getBizCode()).getBizVal();
        storageS3Endpoint = configService.getConfigByCode(SettingsEnum.STORAGE_S3_ENDPOINT.getBizCode()).getBizVal();
    }

    /**
     * 构建 S3 客户端
     *
     * @return S3Client
     */
    private S3Client buildS3Client() {
        getConfigStorage();

        // 创建凭证
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(storageS3AccessKey, storageS3SecretKey);

        // 构建 S3 客户端
        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(storageS3Region));

        // 如果配置了自定义 Endpoint（用于 LocalStack、MinIO 等 S3 兼容存储）
        if (StrUtil.isNotBlank(storageS3Endpoint)) {
            builder.endpointOverride(URI.create(storageS3Endpoint))
                    // 对于 LocalStack 等本地服务，需要使用 path-style 访问
                    .forcePathStyle(true);
        }

        return builder.build();
    }

    /**
     * 根据文件名获取 Content-Type
     *
     * @param objectName 文件名
     * @return Content-Type
     */
    private String getContentType(String objectName) {
        // 首先尝试使用 URLConnection 获取 MIME 类型
        String contentType = URLConnection.guessContentTypeFromName(objectName);
        if (StrUtil.isNotBlank(contentType)) {
            return contentType;
        }
        // 如果无法识别，根据常用扩展名手动匹配
        String extension = objectName.substring(objectName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "bmp" -> "image/bmp";
            case "ico" -> "image/x-icon";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mp3" -> "audio/mpeg";
            case "pdf" -> "application/pdf";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    /**
     * 上传文件流到 S3
     *
     * @param objectName  存储对象的名称（文件在 S3 中的路径）
     * @param inputStream 文件输入流
     * @return 文件路径
     */
    @Override
    public String uploadFileStream(String objectName, InputStream inputStream) {
        S3Client s3Client = buildS3Client();
        try {
            // 读取输入流到字节数组
            byte[] bytes = inputStream.readAllBytes();

            // 根据文件扩展名获取 Content-Type
            String contentType = getContentType(objectName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(storageS3Bucket)
                    .key(objectName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            return objectName;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR + ": " + e.getMessage());
        } finally {
            s3Client.close();
        }
    }

    /**
     * 上传文件到 S3
     *
     * @param objectName 存储对象的名称（文件在 S3 中的路径）
     * @param file       文件对象
     * @return 文件路径
     */
    @Override
    public String uploadFile(String objectName, File file) {
        S3Client s3Client = buildS3Client();
        try {
            // 根据文件扩展名获取 Content-Type
            String contentType = getContentType(objectName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(storageS3Bucket)
                    .key(objectName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            return objectName;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR + ": " + e.getMessage());
        } finally {
            s3Client.close();
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 存储对象的名称
     * @return 文件输入流
     */
    @Override
    public InputStream downloadFile(String objectName) {
        S3Client s3Client = buildS3Client();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(storageS3Bucket)
                    .key(objectName)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR + ": " + e.getMessage());
        }
        // 注意：这里不能关闭 s3Client，因为返回的 InputStream 依赖于它
    }

    /**
     * 删除文件
     *
     * @param objectName 存储对象的名称
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String objectName) {
        S3Client s3Client = buildS3Client();
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(storageS3Bucket)
                    .key(objectName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR + ": " + e.getMessage());
        } finally {
            s3Client.close();
        }
    }
}
