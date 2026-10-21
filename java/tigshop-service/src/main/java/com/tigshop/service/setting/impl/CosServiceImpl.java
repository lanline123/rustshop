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

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.CosService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static com.tigshop.common.constant.ExceptionConstants.SYSTEM_ERROR;

/**
 * 腾讯cos服务
 *
 * @author Tigshop团队
 * @create 2025年01月13日 13:42
 */
@Service
public class CosServiceImpl implements CosService {
    @Resource
    private ConfigService configService;

    private String storageCosSecretKey;
    private String storageCosSecretId;
    private String storageCosRegion;
    private String storageCosBucket;

    private void getConfigStorage() {
        storageCosSecretKey = configService.getConfigByCode(SettingsEnum.STORAGE_COS_SECRET_KEY.getBizCode()).getBizVal();
        storageCosSecretId = configService.getConfigByCode(SettingsEnum.STORAGE_COS_SECRET_ID.getBizCode()).getBizVal();
        storageCosRegion = configService.getConfigByCode(SettingsEnum.STORAGE_COS_REGION.getBizCode()).getBizVal();
        storageCosBucket = configService.getConfigByCode(SettingsEnum.STORAGE_COS_BUCKET.getBizCode()).getBizVal();
    }

    // 初始化 COSClient
    public COSClient initCosClient() {
        getConfigStorage();
        COSCredentials credentials = new BasicCOSCredentials(storageCosSecretId, storageCosSecretKey);

        // 设置区域
        ClientConfig clientConfig = new ClientConfig(new Region(storageCosRegion));
        return new COSClient(credentials, clientConfig);
    }

    // 上传文件到腾讯云 COS
    public String uploadFile(String objectName, File file) {
        COSClient cosClient = initCosClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(storageCosBucket, objectName, file);
            cosClient.putObject(putObjectRequest);
            return objectName;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
    }

    // 上传文件流到 COS
    @Override
    public String uploadFileStream(String objectName, InputStream inputStream) {
        COSClient cosClient = initCosClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(storageCosBucket, objectName, inputStream, null);
            cosClient.putObject(putObjectRequest);

            return objectName;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
    }

    // 下载文件
    public boolean downloadFile(String objectName, File destFile) {
        COSClient cosClient = initCosClient();
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(storageCosBucket, objectName);
            cosClient.getObject(getObjectRequest, destFile);
            // 关闭客户端
            cosClient.shutdown();
            return true;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
    }

    // 获取文件的元数据
    public ObjectMetadata getFileMetadata(String objectName) {
        COSClient cosClient = initCosClient();
        try {
            GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(storageCosBucket, objectName);
            return cosClient.getObjectMetadata(getObjectMetadataRequest);
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
    }

    // 删除文件
    public boolean deleteFile(String objectName) {
        COSClient cosClient = initCosClient();
        try {
            cosClient.deleteObject(storageCosBucket, objectName);
            return true;
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
    }

    // 获取文件列表
    public List<COSObjectSummary> listFiles(String bucketName, String prefix) {
        COSClient cosClient = initCosClient();
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix);
            ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
            return objectListing.getObjectSummaries();
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        } finally {
            // 关闭客户端
            cosClient.shutdown();
        }
    }
}
