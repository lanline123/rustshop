//获取分类详情
import request from "@/utils/request";

export const getImConfig = (params: object) => {
    return request<any>({
        prefix: "/im",
        url: "config/config/detail" ,
        method: "get",
        params
    });
};
export const saveImConfig = (  data: object) => {
    return request({
        prefix: "/im",
        url: "config/config/save" ,
        method: "post",
        data
    });
};
