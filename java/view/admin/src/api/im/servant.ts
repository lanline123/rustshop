
import request from "@/utils/request";
// 获取待接入列表
export const getTransferList = (params:any) => {
    return request<any>({
        prefix: "/im",
        url: "servant/servant/transferList",
        method: "get",
        params
    });
};
