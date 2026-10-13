// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.common;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.UPLOAD_FILE_STORE_ERROR;

/**
 * 对象存储枚举
 *
 * @author Jayce
 * @create 2024年11月12日 09:31
 */
@Getter
@AllArgsConstructor
public enum StorageType {
    STORAGE_LOCAL("0", "storage_local_url", SettingsEnum.STORAGE_LOCAL_URL),
    STORAGE_OSS("1", "storage_oss_url", SettingsEnum.STORAGE_OSS_URL),
    STORAGE_COS("2", "storage_cos_url", SettingsEnum.STORAGE_COS_URL),
    STORAGE_QINIU("3", "storage_qiniu_url", SettingsEnum.STORAGE_QINIU_URL),
    STORAGE_S3("4", "storage_s3_url", SettingsEnum.STORAGE_S3_URL);

    private final String code;
    private final String urlName;
    private final SettingsEnum settingsEnum;



    /**
     * 根据类型编码获取类型
     *
     * @param code 类型编码
     * @return 类型
     */
    public static StorageType fromTypeCode(String code) {
        for (StorageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
    }
}
