import type {AdminLogFilterParams, AdminLogFilterResult} from "@/types/authority/adminLog.d";
import request from "@/utils/request";

export const getAdminLogList = (params: AdminLogFilterParams) => {
    return request<AdminLogFilterResult>({
        url: "authority/adminLog/list",
        method: "get",
        params,
    });
}
// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "authority/adminLog/batch/?type=" + type,
        method: "post",
        data,
    });
}
