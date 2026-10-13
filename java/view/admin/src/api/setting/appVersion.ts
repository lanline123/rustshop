import type {AppVersionFormState} from "@/types/setting/appVersion.d";
import request from "@/utils/request";

export  const getAppVersion = () => {
    return request<AppVersionFormState>({
        url: "setting/appVersion/detail",
        method: "get"
    });
}

export const updateAppVersionLinks = ( data: object) => {
    return request({
        url: "setting/appVersion/update",
        method: "post",
        data
    });
}
