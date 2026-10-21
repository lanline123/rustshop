import request from "@/utils/request";
import type { MailFormState, DingFormState, WXWorkFormState} from "@/types/setting/mail";

export const getMailSettings  = () => {
    return request<MailFormState>({
        url: "setting/config/mailSettings",
        method: "get"
    });
}

export const saveMail  = ( data: object) => {
    return request<MailFormState>({
        url: "setting/config/saveMail",
        method: "post",
        data
    });
}

export const sendTestEmail  = (params: object) => {
    return request<object>({
        url: "setting/config/sendTestEmail",
        method: "post",
        params
    });
}


//钉钉
export const getDingConfig  = () => {
    return request<DingFormState>({
        url: "setting/config/getDingConfig",
        method: "get"
    });
}

export const saveDingConfig  = ( data: object) => {
    return request<DingFormState>({
        url: "setting/config/saveDingConfig",
        method: "post",
        data
    });
}

// 企业微信

export const getWXWorkConfig  = () => {
    return request<WXWorkFormState>({
        url: "setting/config/getWXWorkConfig",
        method: "get"
    });
}

export const saveWXWorkConfig  = ( data: object) => {
    return request<WXWorkFormState>({
        url: "setting/config/saveWXWorkConfig",
        method: "post",
        data
    });
}