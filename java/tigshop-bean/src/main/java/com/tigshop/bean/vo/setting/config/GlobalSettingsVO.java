package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 系统设置-全局设置
 *
 * @author kidd
 * @since 2025/6/6 09:04
 */
@Data
public class GlobalSettingsVO {

    // *** 后台风格 ***

    @ConfigItemField(SettingsEnum.NAV_THEME)
    @Schema(description = "外观")
    private String navTheme;

    @ConfigItemField(SettingsEnum.PRIMARY_COLOR)
    @Schema(description = "主题色")
    private String primaryColor;

    @ConfigItemField(SettingsEnum.LAYOUT)
    @Schema(description = "导航模式")
    private String layout;

    @ConfigItemField(SettingsEnum.ADMIN_LIGHT_LOGO)
    @Schema(description = "后台LOGO")
    private String adminLightLogo;

    @ConfigItemField(SettingsEnum.VERSION_INFO_HIDDEN)
    @Schema(description = "是否隐藏版本信息; 0-否，1-是")
    private Integer versionInfoHidden;

    // *** SEO 设置 ***

    @ConfigItemField(SettingsEnum.SEO_CATEGORY_ENABLE)
    @Schema(description = "商品类目SEO 0 基础链接，1 SEO链接")
    private Integer seoCategoryEnable;

    @ConfigItemField(SettingsEnum.SEO_PRODUCT_ENABLE)
    @Schema(description = "商品SEO 0 基础链接，1 SEO链接")
    private Integer seoProductEnable;

    @ConfigItemField(SettingsEnum.SHOP_TITLE_SUFFIX)
    @Schema(description = "SEO标题后缀")
    private String shopTitleSuffix;

    @ConfigItemField(SettingsEnum.SHOP_TITLE)
    @Schema(description = "首页SEO标题")
    private String shopTitle;

    @ConfigItemField(SettingsEnum.SHOP_KEYWORDS)
    @Schema(description = "首页SEO关键词")
    private String shopKeywords;

    @ConfigItemField(SettingsEnum.SHOP_DESC)
    @Schema(description = "首页SEO描述")
    private String shopDesc;

    @ConfigItemField(SettingsEnum.ICO_IMG)
    @Schema(description = "商城ico")
    private String icoImg;

    // *** 域名设置 ***

    @ConfigItemField(SettingsEnum.CLIENT_DEFAULT_USE)
    @Schema(description = "前台默认跳转 0 pc ,1 h5")
    private Integer clientDefaultUse;

    @ConfigItemField(SettingsEnum.PC_DOMAIN)
    @Schema(description = "PC端域名")
    private String pcDomain;

    @ConfigItemField(SettingsEnum.H5_DOMAIN)
    @Schema(description = "H5端域名")
    private String h5Domain;

    @ConfigItemField(SettingsEnum.ADMIN_DOMAIN)
    @Schema(description = "后台域名")
    private String adminDomain;

    @ConfigItemField(SettingsEnum.AUTO_REDIRECT)
    @Schema(description = "自动跳转H5；0-关闭，1-开启")
    private Integer autoRedirect;

    // *** 上传设置 ***

    @ConfigItemField(SettingsEnum.UPLOAD_MAX_SIZE)
    @Schema(description = "视频上传大小设置，单位：MB")
    private Integer uploadMaxSize;

    @ConfigItemField(SettingsEnum.IMAGE_UPLOAD_MAX_SIZE)
    @Schema(description = "图片上传最大尺寸，单位：MB，支持小数")
    private BigDecimal imageUploadMaxSize;

    @ConfigItemField(SettingsEnum.MATERIAL_UPLOAD_NAME_TYPE)
    @Schema(description = "素材上传命名方式；0-系统随机命名，1-保留原素材名")
    private Integer materialUploadNameType;

    // *** 搜索设置 ***

    @ConfigItemField(SettingsEnum.SEARCH_KEYWORDS)
    @Schema(description = "搜索热门关键字")
    private String searchKeywords;

    @ConfigItemField(SettingsEnum.MSG_HACK_WORD)
    @Schema(description = "敏感词屏蔽")
    private String msgHackWord;

    @ConfigItemField(SettingsEnum.IS_OPEN_PSCWS)
    @Schema(description = "搜索关键词分词；0-关闭，1-开启")
    private Integer isOpenPscws;

    //selectSearchType
    @ConfigItemField(SettingsEnum.SELECT_SEARCH_TYPE)
    @Schema(description = "搜索方式")
    private String selectSearchType;

    // *** 地区设置 ***

    @ConfigItemField(SettingsEnum.SHOP_DEFAULT_REGIONS)
    @Schema(description = "商城默认地区")
    private String shopDefaultRegions;

    @ConfigItemField(SettingsEnum.DEFAULT_COUNTRY)
    @Schema(description = "默认国家")
    private Integer defaultCountry;

    // *** ICO 图标库 ***

    @ConfigItemField(SettingsEnum.ICO_DEFINED_CSS)
    @Schema(description = "自定义ico图标库")
    private String icoDefinedCss;

    // *** 存储设置 ***

    @ConfigItemField(SettingsEnum.STORAGE_TYPE)
    @Schema(description = "存储类型；0-本地存储，1-阿里云 OSS 存储，2-腾讯云 COS 存储，3-七牛云 Kodo 存储")
    private Integer storageType;

    @ConfigItemField(SettingsEnum.STORAGE_LOCAL_URL)
    @Schema(description = "本地 图片访问域名")
    private String storageLocalUrl;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_URL)
    @Schema(description = "OSS 图片访问域名")
    private String storageOssUrl;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_ACCESS_KEY_ID)
    @Schema(description = "OSS AccessKeyId")
    private String storageOssAccessKeyId;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_ACCESS_KEY_SECRET)
    @Schema(description = "OSS AccessKeySecret")
    private String storageOssAccessKeySecret;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_BUCKET)
    @Schema(description = "OSS 空间名称")
    private String storageOssBucket;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_REGION)
    @Schema(description = "OSS 空间地域节点")
    private String storageOssRegion;

    @ConfigItemField(SettingsEnum.STORAGE_COS_URL)
    @Schema(description = "COS 图片访问域名")
    private String storageCosUrl;

    @ConfigItemField(SettingsEnum.STORAGE_COS_SECRET_ID)
    @Schema(description = "COS SecretId")
    private String storageCosSecretId;

    @ConfigItemField(SettingsEnum.STORAGE_COS_SECRET_KEY)
    @Schema(description = "COS KeySecret")
    private String storageCosSecretKey;

    @ConfigItemField(SettingsEnum.STORAGE_COS_BUCKET)
    @Schema(description = "COS 空间名称")
    private String storageCosBucket;

    @ConfigItemField(SettingsEnum.STORAGE_COS_REGION)
    @Schema(description = "COS 空间地域节点")
    private String storageCosRegion;

    @ConfigItemField(SettingsEnum.STORAGE_QINIU_URL)
    @Schema(description = "七牛云 图片访问域名")
    private String storageQiniuUrl;

    @ConfigItemField(SettingsEnum.STORAGE_QINIU_ACCESS_KEY)
    @Schema(description = "七牛云 AccessKey")
    private String storageQiniuAccessKey;

    @ConfigItemField(SettingsEnum.STORAGE_QINIU_SECRET_KEY)
    @Schema(description = "七牛云 SecretKey")
    private String storageQiniuSecretKey;

    @ConfigItemField(SettingsEnum.STORAGE_QINIU_BUCKET)
    @Schema(description = "七牛云 空间名称")
    private String storageQiniuBucket;

    @ConfigItemField(SettingsEnum.STORAGE_QINIU_REGION)
    @Schema(description = "七牛云 空间地域节点")
    private String storageQiniuRegion;

    @ConfigItemField(SettingsEnum.STORAGE_S3_URL)
    @Schema(description = "AWS S3 图片访问域名")
    private String storageS3Url;

    @ConfigItemField(SettingsEnum.STORAGE_S3_ACCESS_KEY)
    @Schema(description = "AWS S3 AccessKey")
    private String storageS3AccessKey;

    @ConfigItemField(SettingsEnum.STORAGE_S3_SECRET_KEY)
    @Schema(description = "AWS S3 SecretKey")
    private String storageS3SecretKey;

    @ConfigItemField(SettingsEnum.STORAGE_S3_BUCKET)
    @Schema(description = "AWS S3 存储桶名称")
    private String storageS3Bucket;

    @ConfigItemField(SettingsEnum.STORAGE_S3_REGION)
    @Schema(description = "AWS S3 区域")
    private String storageS3Region;

    @ConfigItemField(SettingsEnum.STORAGE_S3_ENDPOINT)
    @Schema(description = "AWS S3 Endpoint(可选)")
    private String storageS3Endpoint;

    // *** 多语言设置 ***

    @ConfigItemField(SettingsEnum.LANG_ON)
    @Schema(description = "是否开启多语言；0-否，1-是")
    private Integer langOn;

    @ConfigItemField(SettingsEnum.LANG_TYPE)
    @Schema(description = "接口翻译")
    private Integer langType;

    @ConfigItemField(SettingsEnum.LANG_VOLCENGINE_ACCESS_KEY)
    @Schema(description = "火山翻译AssessKey")
    private String langVolcengineAccessKey;

    @ConfigItemField(SettingsEnum.LANG_VOLCENGINE_SECRET)
    @Schema(description = "火山翻译SecretKey")
    private String langVolcengineSecret;

    @ConfigItemField(SettingsEnum.WORKBUDDY_API_KEY)
    @Schema(description = "WorkBuddy API 密钥（页面仅显示掩码）")
    private String workbuddyApiKey;

    @ConfigItemField(SettingsEnum.WORKBUDDY_API_KEY_EXPIRES_AT)
    @Schema(description = "WorkBuddy API 密钥有效期，Unix 秒")
    private Long workbuddyApiKeyExpiresAt;

    // *** Other ***

    @Schema(description = "国家")
    private List<Region> countries;


    public void encryptData() {
        this.storageOssAccessKeyId = StringUtils.maskMiddleHalf(this.storageOssAccessKeyId);
        this.storageOssAccessKeySecret = StringUtils.maskMiddleHalf(this.storageOssAccessKeySecret);

        this.storageCosSecretId = StringUtils.maskMiddleHalf(this.storageCosSecretId);
        this.storageCosSecretKey = StringUtils.maskMiddleHalf(this.storageCosSecretKey);

        this.storageQiniuAccessKey = StringUtils.maskMiddleHalf(this.storageQiniuAccessKey);
        this.storageQiniuSecretKey = StringUtils.maskMiddleHalf(this.storageQiniuSecretKey);

        this.storageS3AccessKey = StringUtils.maskMiddleHalf(this.storageS3AccessKey);
        this.storageS3SecretKey = StringUtils.maskMiddleHalf(this.storageS3SecretKey);

        this.langVolcengineAccessKey = StringUtils.maskMiddleHalf(this.langVolcengineAccessKey);
        this.langVolcengineSecret = StringUtils.maskMiddleHalf(this.langVolcengineSecret);
        this.workbuddyApiKey = StringUtils.maskMiddleHalf(this.workbuddyApiKey);
    }

}
