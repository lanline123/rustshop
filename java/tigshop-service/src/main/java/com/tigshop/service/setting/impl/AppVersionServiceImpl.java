package com.tigshop.service.setting.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.annotations.settings.ConfigIgnoreField;
import com.tigshop.bean.annotations.settings.ConfigInnerParam;
import com.tigshop.bean.dto.setting.ConfigStorageDTO;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.bean.model.setting.AppVersion;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.param.settings.appversion.AppVersionEditParam;
import com.tigshop.bean.param.settings.appversion.AppVersionParam;
import com.tigshop.bean.param.settings.appversion.AppVersionSaveParam;
import com.tigshop.bean.vo.setting.AppVersionDetailVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.setting.AppVersionMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.setting.*;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.common.constant.ExceptionConstants.UPLOAD_FAIL;
import static com.tigshop.common.constant.ExceptionConstants.UPLOAD_FILE_STORE_ERROR;

/**
* @author kidd
*/
@RequiredArgsConstructor
@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    private final ConfigService configService;

    private final AdminLogService adminLogService;

    @Resource
    private LocalStorageService localStorageService;
    @Resource
    private OssService ossService;
    @Resource
    private CosService cosService;
    @Resource
    private QiniuService qiniuService;
    @Resource
    private AwsS3Service awsS3Service;

    @Override
    public AppVersionDetailVO getDetail() {
        List<SettingsEnum> settings = List.of(
                ANDROID_VERSION, IOS_VERSION, ANDROID_LINK, IOS_LINK
        );
        Map<String, String> settingsMap = configService.getConfigMapBySettings(settings);

        return new AppVersionDetailVO(settingsMap);
    }

    @Override
    public void create(AppVersionSaveParam param) {
        List<SettingsEnum> settings = List.of(
                ANDROID_VERSION, IOS_VERSION, HOT_UPDATE_LINK
        );
        Map<String, String> settingsMap = configService.getConfigMapBySettings(settings);
        Assert.isTrue(CollUtil.isEmpty(settingsMap), () -> new GlobalException("配置已存在，请勿重复添加！"));

        List<ConfigPO> configs;
        try {
            configs = this.reflectConfigs(param);
        } catch (Exception e) {
            throw new GlobalException("设置转换异常");
        }

        List<String> bizCodes = configs.stream().map(ConfigPO::getBizCode).toList();
        configService.lambdaUpdate().in(ConfigPO::getBizCode, bizCodes).remove();

        configService.saveBatch(configs);

        // 添加日志
        Integer adminId = SecurityUtils.getCurrentAdminId();
        AdminLog adminLog = AdminLog.builder()
                .logTime(System.currentTimeMillis())
                .userId(adminId)
                .logInfo("新增APP版本管理: " + param)
                .ipAddress(IpUtils.getIpAddr())
                .build();
        adminLogService.save(adminLog);
    }

    private List<ConfigPO> reflectConfigs(Object obj) throws IllegalAccessException {
        List<ConfigPO> configs = new ArrayList<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object bizVal = field.get(obj);
            if (bizVal != null && field.isAnnotationPresent(ConfigInnerParam.class)) {
                configs.addAll(reflectConfigs(bizVal));
            } else {
                if (field.isAnnotationPresent(ConfigIgnoreField.class)) {
                    continue;
                }
                configs.add(new ConfigPO(field.getName(), String.valueOf(bizVal)));
            }
        }
        return configs;
    }

    @Override
    public void update(AppVersionEditParam param) {
        if (!isValidVersionFormat(param.getAndroidVersion()) ) {
            throw new GlobalException("版本号格式不正确，应为 x.y.z，例如 1.0.0");
        }
        if (!isValidVersionFormat(param.getIosVersion())) {
            throw new GlobalException("版本号格式不正确，应为 x.y.z，例如 1.0.0");
        }

        List<ConfigPO> configs;
        try {
            configs = this.reflectConfigs(param);
        } catch (Exception e) {
            throw new GlobalException("设置转换异常");
        }

        List<String> bizCodes = configs.stream().map(ConfigPO::getBizCode).toList();
        configService.lambdaUpdate().in(ConfigPO::getBizCode, bizCodes).remove();

        configService.saveBatch(configs);

        // 添加日志
        Integer adminId = SecurityUtils.getCurrentAdminId();
        AdminLog adminLog = AdminLog.builder()
                .logTime(StringUtils.getCurrentTime())
                .userId(adminId)
                .logInfo("更新APP版本管理: " + param)
                .ipAddress(IpUtils.getIpAddr())
                .build();
        adminLogService.save(adminLog);
    }

    public ConfigStorageDTO buildConfigStorage() {
        List<SettingsEnum> settings = List.of(
                STORAGE_TYPE, STORAGE_LOCAL_URL, STORAGE_OSS_URL,
                STORAGE_OSS_ACCESS_KEY_ID, STORAGE_OSS_ACCESS_KEY_SECRET,
                STORAGE_OSS_BUCKET, STORAGE_OSS_REGION,
                STORAGE_COS_URL, STORAGE_COS_SECRET_ID,
                STORAGE_COS_SECRET_KEY, STORAGE_COS_BUCKET,
                STORAGE_COS_REGION,
                STORAGE_QINIU_URL, STORAGE_QINIU_ACCESS_KEY,
                STORAGE_QINIU_SECRET_KEY, STORAGE_QINIU_BUCKET,
                STORAGE_QINIU_REGION,
                STORAGE_S3_URL, STORAGE_S3_ACCESS_KEY,
                STORAGE_S3_SECRET_KEY, STORAGE_S3_BUCKET,
                STORAGE_S3_REGION, STORAGE_S3_ENDPOINT
        );

        Map<String, String> storageConfigMap = configService.getConfigMapBySettings(settings);
        return new ConfigStorageDTO(storageConfigMap);
    }

    @Override
    public String upload(MultipartFile file) {
        String originName = file.getOriginalFilename();
        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);
        // 生成随机文件名
        String fileName = StringUtils.getFileName();
        String format = originName.substring(originName.lastIndexOf(StrUtil.DOT));
        String filePath = StrUtil.format("app/update/{}/{}{}", StringUtils.getCurrentYearMonth(), fileName, format);
        try {
            String url = "";
            String apkUrl = "";
            switch (storageType) {
                case STORAGE_LOCAL -> {
                    url = localStorageService.upload(file, filePath);
                    apkUrl = StrUtil.format("{}{}", configStorage.getStorageLocalUrl(), url);
                }
                case STORAGE_OSS -> {
                    url = ossService.uploadFile(file.getInputStream(), filePath);
                    apkUrl = StrUtil.format("{}{}", configStorage.getStorageOssUrl(), url);
                }
                case STORAGE_COS -> {
                    url = cosService.uploadFileStream(filePath, file.getInputStream());
                    apkUrl = StrUtil.format("{}{}", configStorage.getStorageCosUrl(), url);
                }
                case STORAGE_QINIU -> {
                    url = qiniuService.uploadFileStream(filePath, file.getInputStream());
                    apkUrl = StrUtil.format("{}{}", configStorage.getStorageQiniuUrl(), url);
                }
                case STORAGE_S3 -> {
                    url = awsS3Service.uploadFileStream(filePath, file.getInputStream());
                    apkUrl = StrUtil.format("{}{}", configStorage.getStorageS3Url(), url);
                }
                default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
            }
            return apkUrl;
        } catch (IOException e) {
            throw new GlobalException(UPLOAD_FAIL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAppUpdate(AppVersionParam param) {
        AppVersionDetailVO detail = getDetail();
        if ("ios".equals(param.getType())) {
            if (AppVersionServiceImpl.isOldVersion(detail.getIosVersion(), param.getVersion())) {
                return detail.getIosLink();
            }
        }
        if ("android".equals(param.getType())) {
            if (AppVersionServiceImpl.isOldVersion(detail.getAndroidVersion(), param.getVersion())) {
                return detail.getAndroidLink();
            }
        }
        return "";
    }

    public static boolean isOldVersion(String currentVersion, String inputVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] inputParts = inputVersion.split("\\.");

        int length = Math.max(currentParts.length, inputParts.length);
        for (int i = 0; i < length; i++) {
            int curr = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int input = i < inputParts.length ? Integer.parseInt(inputParts[i]) : 0;

            if (input < curr) {
                // 输入版本比当前版本老
                return true;
            } else if (input > curr) {
                // 输入版本比当前版本新
                return false;
            }
        }
        // 版本相同
        return false;
    }

    /**
     * 校验版本格式是否为 x.y.z 的三段数字
     */
    public static boolean isValidVersionFormat(String version) {
        return version != null && version.matches("^\\d+\\.\\d+\\.\\d+$");
    }
}




