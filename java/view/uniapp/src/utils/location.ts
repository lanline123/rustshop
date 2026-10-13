import { useRegionStore } from "@/store/region";
// #ifdef H5
import gcoord from "gcoord";
// #endif

export interface LocationInfo {
    latitude: number;
    longitude: number;
    province: string;
    city: string;
    district: string;
    address: string;
    adcode: string;
}

/**
 * 获取当前位置信息
 */
export const getCurrentRegionInfo = (): Promise<boolean> => {
    return new Promise((resolve) => {
        const regionStore = useRegionStore();

        checkLocationPermission().then(async (hasPermission) => {
            if (!hasPermission) {
                await regionStore.getLocationByIpData();
                resolve(true);
                return;
            }

            uni.getLocation({
                type: "gcj02",
                // H5 定位权限或定位服务异常时不能无限阻塞启动链路。
                timeout: 3000,
                // type: "wgs84",
                altitude: true,
                success: async (res) => {
                    console.log("获取位置成功:", res);

                    let str = `${res.longitude},${res.latitude}`;

                    // #ifdef H5
                    // H5端获取的坐标是WGS84，需要转换为GCJ02
                    const [lng, lat] = gcoord.transform([res.longitude, res.latitude], gcoord.WGS84, gcoord.GCJ02);
                    str = `${lng},${lat}`;
                    console.log("转换后坐标:", str);
                    // #endif

                    // str = `115.863374,28.681535`;

                    regionStore.setRealTimeLocation({ latitude: res.latitude, longitude: res.longitude });
                    // 等区域解析写入 currentRegion/currentLocationInfo 完成后再 resolve，
                    // 保证调用方 await 结束时定位数据已就绪
                    await regionStore.getLocationByLocationData(str);
                    resolve(true);
                },
                fail: (err) => {
                    console.error("获取位置失败:", err);
                    resolve(false);
                }
            });
        });
    });
};

/**
 * 检查定位权限
 */
export const checkLocationPermission = (): Promise<boolean> => {
    return new Promise((resolve) => {
        // #ifdef APP-PLUS
        if (plus.os.name && plus.os.name.toLowerCase() === "ios") {
            // iOS 无 Android 式权限申请 API（plus.android 在 iOS 上为 undefined，
            // 直接调用会导致 Promise 永不 resolve、定位链路挂死）。
            // iOS 定位授权由系统在 uni.getLocation 时弹窗处理，此处放行。
            resolve(true);
        } else {
            const permissionID = "android.permission.ACCESS_FINE_LOCATION";
            plus.android.requestPermissions(
                [permissionID],
                (result) => {
                    if (result.granted && result.granted.length > 0) {
                        resolve(true);
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

        // #ifdef H5
        if (navigator.permissions) {
            navigator.permissions.query({ name: "geolocation" }).then((result) => {
                if (result.state === "granted") {
                    resolve(true);
                } else if (result.state === "prompt") {
                    navigator.geolocation.getCurrentPosition(
                        () => resolve(true),
                        () => resolve(false)
                    );
                } else {
                    resolve(false);
                }
            });
        } else {
            resolve(false);
        }
        // #endif

        // #ifdef MP-WEIXIN || MP-ALIPAY || MP-BAIDU || MP-TOUTIAO
        uni.getSetting({
            success: (res) => {
                if (res.authSetting && res.authSetting["scope.userLocation"]) {
                    resolve(true);
                } else {
                    //  重新请求授权
                    uni.authorize({
                        scope: "scope.userLocation",
                        success: () => {
                            resolve(true);
                        },
                        fail: () => {
                            resolve(false);
                        }
                    });
                }
            },
            fail: () => {
                resolve(false);
            }
        });
        // #endif
    });
};
