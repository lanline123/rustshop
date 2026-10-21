import request from "@/utils/request";

// 获取安装协议
export const getAgreement = () => {
    return request<any>({
        url: "install/index?step=1",
        method: "get"
    });
};

// 系统环境检测
export const getDetection = () => {
    return request<any>({
        url: "install/index?step=2",
        method: "get"
    });
};
// 创建数据
export const updateData = (data: object) => {
    return request({
        url: "install/index?step=3",
        method: "post",
        data
    });
};
