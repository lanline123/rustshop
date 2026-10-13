/**
 * App 端系统权限申请
 *
 * 与标准仓 Tigshop-Uniapp 的同名工具相比，这里不接 appManage store、不弹自定义权限说明：
 * 融合仓的 store 已换成 appVersion（无权限态），也未迁入 components/appPermission 弹窗组件。
 * 调用方按返回值自行提示（见 components/upload/index.vue）。
 */
import { t } from "@/utils/i18n";

/** 永久拒绝后系统弹窗不再唤起，只能引导用户去设置页手动开启 */
const guideToSettings = (content: string, resolve: (granted: boolean) => void) => {
    uni.showModal({
        title: t("权限被禁用"),
        content,
        confirmText: t("去设置"),
        cancelText: t("取消"),
        success: (res) => {
            if (res.confirm) {
                // #ifdef APP-PLUS
                plus.runtime.openURL("app-settings:");
                // #endif
            }
            resolve(false);
        },
        fail: () => resolve(false)
    });
};

/** 申请 Android 运行时权限，返回是否授权 */
const requestAndroidPermission = (permissionID: string, deniedTip: string): Promise<boolean> => {
    return new Promise((resolve) => {
        // #ifdef APP-PLUS
        if (plus.os.name && plus.os.name.toLowerCase() === "ios") {
            // iOS 无 Android 式权限申请 API（plus.android 在 iOS 上为 undefined，
            // 直接调用会导致 Promise 永不 resolve、上传链路挂死）。
            // iOS 相册授权由系统在 uni.chooseImage 时弹窗处理，此处放行。
            resolve(true);
        } else if (plus.android.checkPermission(permissionID) === 0) {
            // 已授权，不必再唤起系统弹窗
            resolve(true);
        } else {
            plus.android.requestPermissions(
                [permissionID],
                (result) => {
                    if (result.granted && result.granted.includes(permissionID)) {
                        resolve(true);
                    } else if (result.deniedAlways && result.deniedAlways.includes(permissionID)) {
                        guideToSettings(deniedTip, resolve);
                    } else {
                        resolve(false);
                    }
                },
                () => {
                    resolve(false);
                }
            );
        }
        // #endif

        // #ifndef APP-PLUS
        resolve(true);
        // #endif
    });
};

/** 申请相册权限 */
export const requestPhotosPermission = (): Promise<boolean> => {
    return requestAndroidPermission("android.permission.READ_EXTERNAL_STORAGE", t("相册权限已被禁用，请在手机设置中开启存储权限后再试。"));
};
