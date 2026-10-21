// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 对象储存配置
 *
 * @author Jayce
 * @create 2024年11月07日 17:08
 */
@Data
@Schema(description = "对象储存配置")
public class ConfigStorageDTO {

    @Schema(description = "存储类型")
    private Integer storageType;

    @Schema(description = "本地存储地址")
    private String storageLocalUrl;

    @Schema(description = "阿里云存储地址")
    private String storageOssUrl;

    @Schema(description = "阿里云访问密钥ID")
    private String storageOssAccessKeyId;

    @Schema(description = "阿里云访问密钥")
    private String storageOssAccessKeySecret;

    @Schema(description = "阿里云存储空间")
    private String storageOssBucket;

    @Schema(description = "阿里云存储区域")
    private String storageOssRegion;

    @Schema(description = "腾讯云存储地址")
    private String storageCosUrl;

    @Schema(description = "腾讯云访问密钥ID")
    private String storageCosSecretId;

    @Schema(description = "腾讯云访问密钥")
    private String storageCosSecretKey;

    @Schema(description = "腾讯云存储空间")
    private String storageCosBucket;

    @Schema(description = "腾讯云存储区域")
    private String storageCosRegion;

    @Schema(description = "七牛云存储地址")
    private String storageQiniuUrl;

    @Schema(description = "七牛云访问密钥AccessKey")
    private String storageQiniuAccessKey;

    @Schema(description = "七牛云访问密钥SecretKey")
    private String storageQiniuSecretKey;

    @Schema(description = "七牛云存储空间")
    private String storageQiniuBucket;

    @Schema(description = "七牛云存储区域")
    private String storageQiniuRegion;

    @Schema(description = "AWS S3存储地址")
    private String storageS3Url;

    @Schema(description = "AWS S3访问密钥")
    private String storageS3AccessKey;

    @Schema(description = "AWS S3密钥")
    private String storageS3SecretKey;

    @Schema(description = "AWS S3存储桶名称")
    private String storageS3Bucket;

    @Schema(description = "AWS S3区域")
    private String storageS3Region;

    @Schema(description = "AWS S3 Endpoint(可选)")
    private String storageS3Endpoint;

    public ConfigStorageDTO(Map<String, String> storageConfigMap) {
        this.storageType = Integer.parseInt(storageConfigMap.get(SettingsEnum.STORAGE_TYPE.getBizCode()));

        this.storageLocalUrl = storageConfigMap.get(SettingsEnum.STORAGE_LOCAL_URL.getBizCode());

        this.storageOssUrl = storageConfigMap.get(SettingsEnum.STORAGE_OSS_URL.getBizCode());
        this.storageOssAccessKeyId = storageConfigMap.get(SettingsEnum.STORAGE_OSS_ACCESS_KEY_ID.getBizCode());
        this.storageOssAccessKeySecret = storageConfigMap.get(SettingsEnum.STORAGE_OSS_ACCESS_KEY_SECRET.getBizCode());
        this.storageOssBucket = storageConfigMap.get(SettingsEnum.STORAGE_OSS_BUCKET.getBizCode());
        this.storageOssRegion = storageConfigMap.get(SettingsEnum.STORAGE_OSS_REGION.getBizCode());

        this.storageCosUrl = storageConfigMap.get(SettingsEnum.STORAGE_COS_URL.getBizCode());
        this.storageCosSecretId = storageConfigMap.get(SettingsEnum.STORAGE_COS_SECRET_ID.getBizCode());
        this.storageCosSecretKey = storageConfigMap.get(SettingsEnum.STORAGE_COS_SECRET_KEY.getBizCode());
        this.storageCosBucket = storageConfigMap.get(SettingsEnum.STORAGE_COS_BUCKET.getBizCode());
        this.storageCosRegion = storageConfigMap.get(SettingsEnum.STORAGE_COS_REGION.getBizCode());

        this.storageQiniuUrl = storageConfigMap.get(SettingsEnum.STORAGE_QINIU_URL.getBizCode());
        this.storageQiniuAccessKey = storageConfigMap.get(SettingsEnum.STORAGE_QINIU_ACCESS_KEY.getBizCode());
        this.storageQiniuSecretKey = storageConfigMap.get(SettingsEnum.STORAGE_QINIU_SECRET_KEY.getBizCode());
        this.storageQiniuBucket = storageConfigMap.get(SettingsEnum.STORAGE_QINIU_BUCKET.getBizCode());
        this.storageQiniuRegion = storageConfigMap.get(SettingsEnum.STORAGE_QINIU_REGION.getBizCode());

        this.storageS3Url = storageConfigMap.get(SettingsEnum.STORAGE_S3_URL.getBizCode());
        this.storageS3AccessKey = storageConfigMap.get(SettingsEnum.STORAGE_S3_ACCESS_KEY.getBizCode());
        this.storageS3SecretKey = storageConfigMap.get(SettingsEnum.STORAGE_S3_SECRET_KEY.getBizCode());
        this.storageS3Bucket = storageConfigMap.get(SettingsEnum.STORAGE_S3_BUCKET.getBizCode());
        this.storageS3Region = storageConfigMap.get(SettingsEnum.STORAGE_S3_REGION.getBizCode());
        this.storageS3Endpoint = storageConfigMap.get(SettingsEnum.STORAGE_S3_ENDPOINT.getBizCode());

    }
}
