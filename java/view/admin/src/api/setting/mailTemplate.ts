import request from "@/utils/request";
import type {MailTemplateFilterState} from "@/types/setting/mailTemplate";


export const getMailTemplateList = () => {
    return request<MailTemplateFilterState[]>({
        url: "setting/mailTemplates/list",
        method: "get"
    });
}

export const updateMailTemplate = (data: object) => {
    return request({
        url: "setting/mailTemplates/update",
        method: "post",
        data
    });
}
