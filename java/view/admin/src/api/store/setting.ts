import request from "@/utils/request";
import type { Response } from "@/types/store/setting"
export const storeSettings = () => {
    return request<Response>({
        url: "setting/config/storeSettings",
        method: "get",
    });
}

export const saveStore = (data: object) => {
    return request<Response>({
        url: "setting/config/saveStore",
        method: "post",
        data
    });
}