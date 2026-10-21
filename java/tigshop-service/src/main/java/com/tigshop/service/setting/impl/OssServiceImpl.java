// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.VoidResult;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.OssService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import static com.tigshop.common.constant.Constants.HTTPS;
import static com.tigshop.common.constant.ExceptionConstants.SYSTEM_ERROR;

/**
 * 阿里云对象存储
 *
 * @author Tigshop团队
 * @create 2024年11月14日 10:29
 */
@Service
public class OssServiceImpl implements OssService {
    @Resource
    private ConfigService configService;

    private String storageOssRegion;
    private String storageOssBucket;
    private String storageOssAccessKeyId;
    private String storageOssAccessKeySecret;

    /**
     * 上传文件到 OSS
     *
     * @param filePath   文件的路径
     * @param objectName 存储在 OSS 中的对象名
     * @return 文件访问 URL
     */
    @Override
    public String uploadFile(String filePath, String objectName) {
        try {
            OSS ossClient = buildOssClient();
            PutObjectRequest putObjectRequest = new PutObjectRequest(storageOssBucket, objectName, new File(filePath));
            ossClient.putObject(putObjectRequest);
            return generateFileUrl(objectName, ossClient);
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 上传字节数组到 OSS
     *
     * @param content    字节数组
     * @param objectName 存储在 OSS 中的对象名
     * @return 文件访问 URL
     */
    @Override
    public String uploadBytes(byte[] content, String objectName) {
        try {
            OSS ossClient = buildOssClient();
            ossClient.putObject(storageOssBucket, objectName, new ByteArrayInputStream(content));
            return generateFileUrl(objectName, ossClient);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 存储在 OSS 中的对象名
     * @return 文件流
     */
    @Override
    public InputStream downloadFile(String objectName) {
        try {
            OSS ossClient = buildOssClient();
            OSSObject ossObject = ossClient.getObject(storageOssBucket, objectName);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 存储在 OSS 中的对象名
     */
    @Override
    public ResponseMessage deleteFile(String objectName) {
        try {
            OSS ossClient = buildOssClient();
            VoidResult voidResult = ossClient.deleteObject(storageOssBucket, objectName);
            return voidResult.getResponse();
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 构建 OSS 客户端
     */
    private OSS buildOssClient() {
        getConfigStorage();
        return new OSSClientBuilder().build(
                HTTPS + storageOssRegion,
                storageOssAccessKeyId,
                storageOssAccessKeySecret
        );
    }

    /**
     * 获取配置信息
     */
    private void getConfigStorage() {
        storageOssRegion = configService.getConfigByCode(SettingsEnum.STORAGE_OSS_REGION.getBizCode()).getBizVal();
        storageOssBucket = configService.getConfigByCode(SettingsEnum.STORAGE_OSS_BUCKET.getBizCode()).getBizVal();
        storageOssAccessKeyId = configService.getConfigByCode(SettingsEnum.STORAGE_OSS_ACCESS_KEY_ID.getBizCode()).getBizVal();
        storageOssAccessKeySecret = configService.getConfigByCode(SettingsEnum.STORAGE_OSS_ACCESS_KEY_SECRET.getBizCode()).getBizVal();
    }

    /**
     * 生成文件的访问 URL，带过期时间
     *
     * @param objectName 存储在 OSS 中的对象名
     * @param ossClient  OSS 客户端实例
     * @return 文件访问 URL
     */
    @Override
    public String generateFileUrl(String objectName, OSS ossClient) {
        // 1小时后过期
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        URL url = ossClient.generatePresignedUrl(storageOssBucket, objectName, expiration);
        return url.toString();
    }

    @Override
    public String uploadFile(InputStream inputStream, String filePath) {
        OSS ossClient = buildOssClient();
        try {
            // 上传文件到指定的 Bucket
            ossClient.putObject(storageOssBucket, filePath, inputStream);

            // 返回文件 URL
            return filePath;
        } finally {
            // 关闭 OSSClient
            ossClient.shutdown();
        }
    }
}
