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

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.QiniuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.*;

import static com.tigshop.common.constant.ExceptionConstants.SYSTEM_ERROR;

/**
 * 七牛云对象存储
 *
 * @author Tigshop团队
 * @create 2025年12月05日
 */
@Service
public class QiniuServiceImpl implements QiniuService {
    @Resource
    private ConfigService configService;

    private String storageQiniuAccessKey;
    private String storageQiniuSecretKey;
    private String storageQiniuBucket;
    private String storageQiniuRegion;

    /**
     * 上传文件到七牛云
     *
     * @param filePath   文件的路径
     * @param objectName 存储在七牛云中的对象名
     * @return 文件访问路径
     */
    @Override
    public String uploadFile(String filePath, String objectName) {
        try {
            Auth auth = buildAuth();
            String upToken = auth.uploadToken(storageQiniuBucket);
            
            Configuration cfg = buildConfiguration();
            UploadManager uploadManager = new UploadManager(cfg);
            
            Response response = uploadManager.put(filePath, objectName, upToken);
            if (response.isOK()) {
                return objectName;
            } else {
                throw new GlobalException(SYSTEM_ERROR);
            }
        } catch (QiniuException e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 上传字节数组到七牛云
     *
     * @param content    字节数组
     * @param objectName 存储在七牛云中的对象名
     * @return 文件访问路径
     */
    @Override
    public String uploadBytes(byte[] content, String objectName) {
        try {
            Auth auth = buildAuth();
            String upToken = auth.uploadToken(storageQiniuBucket);
            
            Configuration cfg = buildConfiguration();
            UploadManager uploadManager = new UploadManager(cfg);
            
            Response response = uploadManager.put(content, objectName, upToken);
            if (response.isOK()) {
                return objectName;
            } else {
                throw new GlobalException(SYSTEM_ERROR);
            }
        } catch (QiniuException e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 上传文件流到七牛云
     *
     * @param objectName  存储在七牛云中的对象名
     * @param inputStream 文件流
     * @return 文件访问路径
     */
    @Override
    public String uploadFileStream(String objectName, InputStream inputStream) {
        try {
            Auth auth = buildAuth();
            String upToken = auth.uploadToken(storageQiniuBucket);
            
            Configuration cfg = buildConfiguration();
            UploadManager uploadManager = new UploadManager(cfg);
            
            Response response = uploadManager.put(inputStream, objectName, upToken, null, null);
            if (response.isOK()) {
                return objectName;
            } else {
                throw new GlobalException(SYSTEM_ERROR);
            }
        } catch (QiniuException e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 存储在七牛云中的对象名
     * @return 文件流
     */
    @Override
    public InputStream downloadFile(String objectName) {
        try {
            String fileUrl = generateFileUrl(objectName);
            java.net.URL url = new java.net.URL(fileUrl);
            return url.openStream();
        } catch (Exception e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 存储在七牛云中的对象名
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String objectName) {
        try {
            Auth auth = buildAuth();
            Configuration cfg = buildConfiguration();
            BucketManager bucketManager = new BucketManager(auth, cfg);
            
            bucketManager.delete(storageQiniuBucket, objectName);
            return true;
        } catch (QiniuException e) {
            throw new GlobalException(SYSTEM_ERROR);
        }
    }

    /**
     * 生成文件的访问 URL
     *
     * @param objectName 存储在七牛云中的对象名
     * @return 文件访问 URL
     */
    @Override
    public String generateFileUrl(String objectName) {
        getConfigStorage();
        Auth auth = buildAuth();
        // 获取七牛云配置的访问域名
        String storageQiniuUrl = configService.getConfigByCode(SettingsEnum.STORAGE_QINIU_URL.getBizCode()).getBizVal();
        
        // 构建文件URL，这里假设使用私有空间，需要生成带签名的URL
        // 如果是公有空间，可以直接返回：storageQiniuUrl + "/" + objectName
        String baseUrl = storageQiniuUrl + "/" + objectName;
        
        // 设置过期时间为1小时
        long expireInSeconds = 3600;
        
        try {
            // 生成带签名的私有空间访问链接
            return auth.privateDownloadUrl(baseUrl, expireInSeconds);
        } catch (Exception e) {
            // 如果是公有空间，签名可能会失败，直接返回基础URL
            return baseUrl;
        }
    }

    /**
     * 构建 Auth 认证对象
     */
    private Auth buildAuth() {
        getConfigStorage();
        return Auth.create(storageQiniuAccessKey, storageQiniuSecretKey);
    }

    /**
     * 构建配置对象
     */
    private Configuration buildConfiguration() {
        getConfigStorage();
        
        // 根据区域配置选择对应的 Region
        Region region;
        switch (storageQiniuRegion) {
            case "华东" -> region = Region.huadong();
            case "华北" -> region = Region.huabei();
            case "华南" -> region = Region.huanan();
            case "北美" -> region = Region.beimei();
            case "东南亚" -> region = Region.xinjiapo();
            default -> region = Region.autoRegion(); // 自动识别区域
        }
        
        return new Configuration(region);
    }

    /**
     * 获取配置信息
     */
    private void getConfigStorage() {
        storageQiniuAccessKey = configService.getConfigByCode(SettingsEnum.STORAGE_QINIU_ACCESS_KEY.getBizCode()).getBizVal();
        storageQiniuSecretKey = configService.getConfigByCode(SettingsEnum.STORAGE_QINIU_SECRET_KEY.getBizCode()).getBizVal();
        storageQiniuBucket = configService.getConfigByCode(SettingsEnum.STORAGE_QINIU_BUCKET.getBizCode()).getBizVal();
        storageQiniuRegion = configService.getConfigByCode(SettingsEnum.STORAGE_QINIU_REGION.getBizCode()).getBizVal();
    }
}

