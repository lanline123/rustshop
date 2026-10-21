import request from "@/utils/request";
import type { LicensedFormState, BaseLicensedData, MailFormResult } from "@/types/setting/licensed";
export const getLicensed = () => {
    return request<LicensedFormState>({
        url: "setting/licensed/index",
        method: "get"
    });
}

export const licensedSave  = ( data: BaseLicensedData) => {
    return request({
        url: "setting/licensed/saveLicensed",
        method: "post",
        data
    });
}

export const licensedUpdate  = ( data: MailFormResult) => {
    return request({
        url: "setting/licensed/update",
        method: "post",
        data
    });
}
